package com.exam.review.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import com.exam.product.Product;
import com.exam.product.ProductRepository;
import com.exam.review.Review;
import com.exam.review.ReviewRepository;
import com.exam.review.ai.vector.ReviewAnalysisVectorRepository;
import com.exam.review.ai.vector.ReviewAnalysisVectorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAnalysisService {

	private final ReviewRepository reviewRepository;
	private final ProductRepository productRepository;

	private final ChatClient chatClient;
	//벡터DB 사용을 위한 의존성 주입
	private final ReviewAnalysisVectorRepository reviewAnalysisVectorRepository;
	private final ReviewAnalysisVectorService vectorService;

	//@Cacheable(value = "reviewAnalysis", key = "#productCode")
	public ReviewAnalysisResponseDTO analyzeReviews(String productCode) {
		log.info("Analyzing reviews for product code: {}", productCode);

		// 상품 정보 가져오기
		Product product = productRepository.findByProductCode(productCode);
		if (product == null) {
			throw new RuntimeException("상품을 찾을 수 없습니다: " + productCode);
		}

		// 리뷰 목록 가져오기
		List<Review> reviews = reviewRepository.findByProductCode(productCode);

		if (reviews.isEmpty()) {
			log.info("No reviews found for product: {}", productCode);
			return createEmptyResponse(product);
		}

		// 평균 별점 계산
		double averageRating = reviewRepository.getAverageRating(productCode);

		// 리뷰 텍스트 결합
		String reviewTexts = reviews.stream()
			.map(r -> "별점: " + r.getRating() + "/5, 리뷰: " + r.getReviewText())
			.collect(Collectors.joining("\n"));

		// AI 모델에 리뷰 분석 요청
		String aiResponse = requestAiAnalysis(product.getProductName(), reviewTexts);

		// AI 응답을 JSON (DTO) 형태로 파싱
		ReviewAnalysisResponseDTO response = parseAiResponse(aiResponse);
		response.setProductCode(productCode);
		response.setProductName(product.getProductName());
		response.setAverageRating(averageRating);
		response.setReviewCount(reviews.size());
		// 시맨틱 검색을 위한 벡터 저장 - 이 블록 추가

		try {
			vectorService.saveReviewAnalysisVector(
				response.hashCode(), // 또는 더 나은 ID 생성 전략
				productCode,
				reviewTexts,
				response.getSummary(),
				response.getKeyPositivePoints(),
				response.getKeyNegativePoints()
			);
			log.info("Vector embeddings saved for product: {}", productCode);
		} catch (Exception e) {
			log.error("Error saving vector embeddings: {}", e.getMessage(), e);
			// 벡터 저장 실패해도 전체 작업은 실패하지 않도록 함
		}
		log.info("Review analysis completed for product: {}", productCode);
		return response;
	}

	private ReviewAnalysisResponseDTO createEmptyResponse(Product product) {
		return ReviewAnalysisResponseDTO.builder()
			.productCode(product.getProductCode())
			.productName(product.getProductName())
			.reviewCount(0)
			.averageRating(0)
			.summary("아직 리뷰가 없습니다.")
			.keyPositivePoints(Collections.emptyList())
			.keyNegativePoints(Collections.emptyList())
			.build();
	}

	private String requestAiAnalysis(String productName, String reviewTexts) {
		log.info("Requesting AI analysis for product: {}", productName);

		// 시스템 메시지 생성
		// AI의 역할/행동 지침을 사전에 설정하는 프롬프트 객체
		SystemMessage systemMessage = new SystemMessage(
			"당신은 제품 리뷰 분석 전문가입니다. 사용자가 제공하는 제품 리뷰들을 분석하여 다음 정보를 JSON 형식으로 반환하세요:\n" +
				"1. sentimentAnalysis: 긍정/부정/중립 감정 비율 (예: {\"positive\": 0.7, \"negative\": 0.2, \"neutral\": 0.1})\n" +
				"2. keyPositivePoints: 주요 긍정적 포인트 리스트 (최대 5개)\n" +
				"3. keyNegativePoints: 주요 부정적 포인트 리스트 (최대 5개)\n" +
				"4. summary: 전체 리뷰 요약 (100자 내외)\n" +
				"5. recommendations: 구매자에게 도움이 될 추천사항 리스트 (최대 3개)\n" +
				"6. reviewCategories: 다음 5가지 카테고리로 리뷰를 분류하고 각 카테고리별 언급 횟수를 계산하세요: \n" +
				"   - {\"category\": \"맛/품질\", \"count\": 0, \"emoji\": \"😋\"}\n" +
				"   - {\"category\": \"가성비\", \"count\": 0, \"emoji\": \"💰\"}\n" +
				"   - {\"category\": \"신선도\", \"count\": 0, \"emoji\": \"🌱\"}\n" +
				"   - {\"category\": \"양/크기\", \"count\": 0, \"emoji\": \"🍽️\"}\n" +
				"   - {\"category\": \"주차편의성\", \"count\": 0, \"emoji\": \"🅿️\"}\n" +
				"JSON 형식으로 응답해야 합니다. 다른 텍스트나 설명은 포함하지 마세요."
		);

		// 사용자 메시지 생성
		// 사용자의 실제 요청을 보내는 객체
		UserMessage userMessage = new UserMessage(
			"다음은 '" + productName + "' 제품에 대한 사용자 리뷰입니다. 분석해주세요:\n\n" + reviewTexts
		);

		// 프롬프트 생성
		List<Message> messages = Arrays.asList(systemMessage, userMessage);

		// SystemMessage + UserMessage를 묶은 프롬프트
		// ChatClient가 처리할 수 있도록 포맷을 갖춘 것
		// AI에게 프롬프트를 전달하고 응답을 받아 결과를 JSON 으로 반환함
		// 내부적으로 Spring AI 가 사용하는 LLM에 요청이 전송됨
		Prompt prompt = new Prompt(messages);

		try {
			// AI 호출
			return chatClient.call(prompt).getResult().getOutput().getContent();
		} catch (Exception e) {
			log.error("Error during AI analysis: {}", e.getMessage(), e);
			throw new RuntimeException("AI 분석 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private ReviewAnalysisResponseDTO parseAiResponse(String jsonResponse) {
		try {
			log.info("Parsing AI response");
			ReviewAnalysisResponseDTO dto = new ReviewAnalysisResponseDTO();
			JSONObject json = new JSONObject(jsonResponse);

			// 감정 분석
			if (json.has("sentimentAnalysis")) {
				JSONObject sentiment = json.getJSONObject("sentimentAnalysis");
				Map<String, Double> sentimentMap = new HashMap<>();
				for (String key : sentiment.keySet()) {
					sentimentMap.put(key, sentiment.getDouble(key));
				}
				dto.setSentimentAnalysis(sentimentMap);
			}

			// 긍정적 포인트
			if (json.has("keyPositivePoints")) {
				JSONArray positivePoints = json.getJSONArray("keyPositivePoints");
				List<String> positiveList = new ArrayList<>();
				for (int i = 0; i < positivePoints.length(); i++) {
					positiveList.add(positivePoints.getString(i));
				}
				dto.setKeyPositivePoints(positiveList);
			}

			// 부정적 포인트
			if (json.has("keyNegativePoints")) {
				JSONArray negativePoints = json.getJSONArray("keyNegativePoints");
				List<String> negativeList = new ArrayList<>();
				for (int i = 0; i < negativePoints.length(); i++) {
					negativeList.add(negativePoints.getString(i));
				}
				dto.setKeyNegativePoints(negativeList);
			}

			// 요약
			if (json.has("summary")) {
				dto.setSummary(json.getString("summary"));
			}

			// 리뷰 카테고리 파싱
			if (json.has("reviewCategories")) {
				JSONArray categories = json.getJSONArray("reviewCategories");
				List<ReviewCategoryDTO> categoryList = new ArrayList<>();

				for (int i = 0; i < categories.length(); i++) {
					JSONObject category = categories.getJSONObject(i);
					ReviewCategoryDTO categoryDTO = new ReviewCategoryDTO(
						category.getString("category"),
						category.getInt("count"),
						category.has("emoji") ? category.getString("emoji") : ""
					);
					categoryList.add(categoryDTO);
				}

				dto.setReviewCategories(categoryList);
			}

			return dto;
		} catch (Exception e) {
			log.error("Error parsing AI response: {}", e.getMessage(), e);
			throw new RuntimeException("AI 응답 파싱 중 오류 발생: " + e.getMessage());
		}
	}
}