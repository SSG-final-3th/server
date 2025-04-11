package com.exam.chatbot;

import com.exam.product.ProductDTO;
import com.exam.product.ProductService;
import com.exam.review.ai.vector.ReviewAnalysisVector;
import com.exam.review.ai.vector.ReviewAnalysisVectorRepository;
import com.exam.review.ai.vector.ReviewAnalysisVectorService;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

	private final ChatClient chatClient;
	private final EmbeddingClient embeddingClient;
	private final ProductService productService;
	private final ReviewAnalysisVectorRepository vectorRepository;

	// 간단한 세션 관리 (실제 프로덕션에서는 Redis 등을 사용하는 것이 좋음)
	private final Map<String, List<Map<String, String>>> chatHistory = new ConcurrentHashMap<>();
	private final ReviewAnalysisVectorService reviewAnalysisVectorService;

	public ChatbotResponseDTO processMessage(ChatbotRequestDTO request) {
		log.info("Processing chatbot message: {}, sessionId: {}", request.getMessage(), request.getSessionId());

		// 세션 ID가 없는 경우 생성
		String sessionId = Optional.ofNullable(request.getSessionId())
			.orElse(UUID.randomUUID().toString());

		// 세션 히스토리 가져오기 또는 새로 생성
		List<Map<String, String>> history = chatHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

		// 사용자 메시지를 히스토리에 추가
		Map<String, String> userMessage = new HashMap<>();
		userMessage.put("role", "user");
		userMessage.put("content", request.getMessage());
		history.add(userMessage);

		// 제품 관련 질문인지 확인 (벡터 검색)
		String contextInfo = "";
		if (request.getMessage().length() > 5) { // 너무 짧은 메시지는 벡터 검색에서 제외
			contextInfo = getRelevantProductInfo(request.getMessage());
		}

		// GPT에 메시지 전송
		String response = sendMessageToGPT(history, contextInfo, request.getProductCode());

		// GPT 응답을 히스토리에 추가
		Map<String, String> assistantMessage = new HashMap<>();
		assistantMessage.put("role", "assistant");
		assistantMessage.put("content", response);
		history.add(assistantMessage);

		// 응답이 제품 추천인지 확인
		boolean isProductRecommendation = response.contains("제품 추천:") || response.contains("추천 제품:") ||
			response.contains("product_recommendation");

		List<ProductSuggestionDTO> suggestedProducts = new ArrayList<>();
		if (isProductRecommendation) {
			suggestedProducts = extractProductRecommendations(response);
			// 실제 제품 정보로 보강
			suggestedProducts = enrichProductRecommendations(suggestedProducts);
		}

		// 히스토리 크기 관리 (너무 길어지면 오래된 메시지 제거)
		if (history.size() > 10) {
			history = history.subList(history.size() - 10, history.size());
			chatHistory.put(sessionId, history);
		}

		return ChatbotResponseDTO.builder()
			.message(cleanResponseText(response))
			.sessionId(sessionId)
			.isProductRecommendation(isProductRecommendation)
			.suggestedProducts(suggestedProducts)
			.build();
	}

	private String getRelevantProductInfo(String userQuery) {
		try {
			// 벡터 검색을 위해 쿼리 임베딩 생성
			float[] queryEmbedding = reviewAnalysisVectorService.createEmbedding(userQuery);
			String vectorStr = reviewAnalysisVectorService.formatVector(queryEmbedding);

			// 기존 리포지토리의 메서드를 활용한 검색
			List<Object[]> results = vectorRepository.findSimilarByEmbedding(vectorStr, 3);

			if (results == null || results.isEmpty()) {
				// 벡터 검색에서 결과가 없으면 키워드 기반으로 제품 검색
				List<String> keywords = extractKeywords(userQuery);
				if (!keywords.isEmpty()) {
					return getProductInfoByKeywords(keywords);
				}
				return "";
			}

			StringBuilder contextBuilder = new StringBuilder("관련 제품 정보:\n");
			for (Object[] result : results) {
				Integer analysisId = (Integer) result[0];
				String productCode = (String) result[1];
				Double similarity = (Double) result[2];

				ProductDTO product = productService.getProductByCode(productCode);
				if (product != null) {
					contextBuilder.append("제품명: ").append(product.getProductName())
						.append("\n제품코드: ").append(productCode)
						.append("\n카테고리: ").append(product.getCategory())
						.append("\n가격: ").append(product.getPrice()).append("원")
						.append("\n평균 평점: ").append(product.getAverageRating());

					// 분석 ID로 추가 정보 가져오기 (요약, 긍정/부정 포인트)
					// 이 부분은 필요에 따라 ReviewAnalysisDAO나 다른 리포지토리에서 가져와야 함
					ReviewAnalysisVector vector = vectorRepository.findById(analysisId).orElse(null);
					if (vector != null) {
						// 요약과 포인트는 다른 테이블에서 가져와야 할 수 있음
						// 일단 임시로 상품 설명을 요약으로 대체
						contextBuilder.append("\n요약: ").append(product.getDescription());
						contextBuilder.append("\n유사도: ").append(String.format("%.2f", similarity));
					}

					contextBuilder.append("\n\n");
				}
			}

			return contextBuilder.toString();
		} catch (Exception e) {
			log.error("Error during vector search: {}", e.getMessage(), e);
			return "";
		}
	}

	private List<String> extractKeywords(String userQuery) {
		// 간단한 키워드 추출 로직 (실제로는 좀 더 정교한 로직을 구현하는 것이 좋음)
		List<String> stopWords = Arrays.asList("은", "는", "이", "가", "을", "를", "에", "의", "로", "으로", "에서", "세요", "해주세요", "해줘", "알려줘", "찾아줘", "추천해줘");
		String[] words = userQuery.toLowerCase().split("\\s+");
		return Arrays.stream(words)
			.filter(word -> word.length() > 1) // 1글자 단어 제외
			.filter(word -> !stopWords.contains(word)) // 불용어 제외
			.collect(Collectors.toList());
	}

	private String getProductInfoByKeywords(List<String> keywords) {
		// 키워드 기반으로 제품 검색하는 로직
		StringBuilder contextBuilder = new StringBuilder("키워드 관련 제품 정보:\n");
		Set<String> addedProductCodes = new HashSet<>(); // 중복 방지

		for (String keyword : keywords) {
			List<ProductDTO> products = productService.getProductsByName(keyword);
			if (products.size() > 0) {
				for (int i = 0; i < Math.min(products.size(), 2); i++) { // 키워드당 최대 2개 제품만
					ProductDTO product = products.get(i);
					if (addedProductCodes.contains(product.getProductCode())) {
						continue; // 이미 추가된 제품은 스킵
					}

					contextBuilder.append("제품명: ").append(product.getProductName())
						.append("\n제품코드: ").append(product.getProductCode())
						.append("\n카테고리: ").append(product.getCategory())
						.append("\n가격: ").append(product.getPrice()).append("원")
						.append("\n평균 평점: ").append(product.getAverageRating())
						.append("\n설명: ").append(product.getDescription())
						.append("\n\n");

					addedProductCodes.add(product.getProductCode());
				}
			}

			// 키워드로 카테고리 검색
			List<ProductDTO> categoryProducts = productService.getProductsByCategory(keyword);
			if (categoryProducts.size() > 0) {
				contextBuilder.append("카테고리 '").append(keyword).append("'의 인기 제품:\n");
				for (int i = 0; i < Math.min(categoryProducts.size(), 3); i++) {
					ProductDTO product = categoryProducts.get(i);
					if (addedProductCodes.contains(product.getProductCode())) {
						continue; // 이미 추가된 제품은 스킵
					}

					contextBuilder.append("- ").append(product.getProductName())
						.append(" (").append(product.getPrice()).append("원)")
						.append(" 평점: ").append(product.getAverageRating())
						.append("\n");

					addedProductCodes.add(product.getProductCode());
				}
				contextBuilder.append("\n");
			}
		}

		if (addedProductCodes.isEmpty()) {
			return ""; // 관련 제품이 없으면 빈 문자열 반환
		}

		return contextBuilder.toString();
	}

	private String sendMessageToGPT(List<Map<String, String>> history, String contextInfo, String productCode) {
		// 시스템 메시지 생성
		SystemMessage systemMessage = new SystemMessage(
			"당신은 '몰앤몰'이라는 쇼핑몰의 AI 쇼핑 도우미입니다. 다음 지침을 따라주세요:\n" +
				"1. 항상 예의 바르고 친절하게 응답하세요. '고객님'이라는 존칭을 사용하세요.\n" +
				"2. 제품 추천 시 고객 질문과 관련된 제품 정보를 벡터 데이터베이스에서 검색하여 활용하세요.\n" +
				"3. 제품 추천을 요청받으면 일반 대화 응답을 먼저 제공한 후, 별도로 <product_recommendation>태그 안에 JSON 형식으로 제품 정보를 포함하세요. 이 태그는 내부 처리용으로, 실제 고객에게는 보이지 않습니다.\n" +
				"4. JSON 형식: {\"productCode\": \"코드\", \"productName\": \"제품명\", \"price\": \"가격\"}\n" +
				"5. 제품 리뷰 정보를 질문받으면 자연스러운 대화 형태로 응답하고, 데이터 처리를 위해 <review_summary> 태그를 별도로 추가하세요.\n" +
				"6. 태그는 반드시 응답의 마지막에 배치하고, 일반 대화 부분과 구분되도록 작성하세요.\n" +
				"7. 고객이 특정 제품의 장점이나 단점을 물으면 자연스러운 문장으로 설명하세요.\n" +
				"8. 배송 관련 질문에는 '당일 오전 11시 이전 주문 시 당일 배송 가능'이라고 안내해주세요.\n" +
				"9. 제품 카테고리는 '채소', '과일', '정육/계란', '수산물', '간편식', '유제품'이 있습니다.\n" +
				"10. 고객 질문에 대한 답변은 간결하고 정확하게 제공하세요.\n" +
				"11. <product_recommendation>과 <review_summary> 같은 태그는 시스템 처리용이므로 태그 자체에 대한 설명이나 언급은 하지 마세요.\n" +
				(contextInfo.isEmpty() ? "" : "\n\n관련 제품 정보:\n" + contextInfo)
		);

		// 사용자 메시지 준비
		List<Message> messages = new ArrayList<>();
		messages.add(systemMessage);

		// 히스토리 메시지 추가 (최근 5개만)
		int startIdx = Math.max(0, history.size() - 5);
		for (int i = startIdx; i < history.size(); i++) {
			Map<String, String> msg = history.get(i);
			if ("user".equals(msg.get("role"))) {
				messages.add(new UserMessage(msg.get("content")));
			} else {
				// 이전 AI 응답은 시스템 메시지처럼 처리
				messages.add(new SystemMessage("이전 응답: " + msg.get("content")));
			}
		}

		// 특정 제품에 대한 질문인 경우 추가 컨텍스트
		if (productCode != null && !productCode.isEmpty()) {
			ProductDTO product = productService.getProductByCode(productCode);
			if (product != null) {
				messages.add(new SystemMessage("사용자가 문의 중인 제품: " + product.getProductName() +
					" (코드: " + product.getProductCode() + ", 카테고리: " + product.getCategory() +
					", 가격: " + product.getPrice() + "원)"));
			}
		}

		// 프롬프트 생성 및 AI 호출
		Prompt prompt = new Prompt(messages);
		try {
			return chatClient.call(prompt).getResult().getOutput().getContent();
		} catch (Exception e) {
			log.error("Error in AI response: {}", e.getMessage(), e);
			return "죄송합니다. 현재 시스템에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
		}
	}

	private List<ProductSuggestionDTO> enrichProductRecommendations(List<ProductSuggestionDTO> suggestions) {
		List<ProductSuggestionDTO> enrichedSuggestions = new ArrayList<>();

		for (ProductSuggestionDTO suggestion : suggestions) {
			if (suggestion.getProductCode() != null && !suggestion.getProductCode().isEmpty()) {
				// 제품 코드로 실제 제품 정보 조회
				ProductDTO productDTO = productService.getProductByCode(suggestion.getProductCode());
				if (productDTO != null) {
					suggestion.setProductName(productDTO.getProductName());
					suggestion.setCategory(productDTO.getCategory());
					suggestion.setDescription(productDTO.getDescription());
					suggestion.setPrice(productDTO.getPrice());
					suggestion.setImage(productDTO.getImage());
					suggestion.setAverageRating(productDTO.getAverageRating());

					enrichedSuggestions.add(suggestion);
				}
			} else if (suggestion.getProductName() != null && !suggestion.getProductName().isEmpty()) {
				// 제품명으로 검색 (정확한 매치가 어려울 수 있음)
				List<ProductDTO> products = productService.getProductsByName(suggestion.getProductName());
				if (!products.isEmpty()) {
					ProductDTO productDTO = products.get(0); // 첫 번째 매치 사용
					suggestion.setProductCode(productDTO.getProductCode());
					suggestion.setCategory(productDTO.getCategory());
					suggestion.setDescription(productDTO.getDescription());
					suggestion.setPrice(productDTO.getPrice());
					suggestion.setImage(productDTO.getImage());
					suggestion.setAverageRating(productDTO.getAverageRating());
				}
				enrichedSuggestions.add(suggestion);
			}
		}

		return enrichedSuggestions;
	}

	// 세션 정리 (필요시 호출)
	public void clearSession(String sessionId) {
		chatHistory.remove(sessionId);
		log.info("Session cleared: {}", sessionId);
	}

	// JSON 응답에서 텍스트 부분만 추출 (개선된 버전)
	private String cleanResponseText(String response) {
		// 모든 <tag>내용</tag> 형식의 태그 제거
		String cleaned = response.replaceAll("<product_recommendation>.*?</product_recommendation>", "")
			.replaceAll("<review_summary>.*?</review_summary>", "");

		// 빈 태그 제거 (내용이 없는 태그)
		cleaned = cleaned.replaceAll("<[^>]*>\\s*</[^>]*>", "");

		// 다른 태그들도 제거
		cleaned = cleaned.replaceAll("<[^>]*>", "");

		// 연속된 여러 줄바꿈 정리
		cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");

		// 마지막의 공백 제거
		cleaned = cleaned.trim();

		// JSON 형식으로 보이는 부분 제거
		if (cleaned.contains("{") && cleaned.contains("}")) {
			int startIdx = cleaned.indexOf("{");
			int endIdx = cleaned.lastIndexOf("}") + 1;

			// JSON이 문장 중간에 있는지 확인
			if (startIdx > 0 && endIdx < cleaned.length()) {
				// 문장 중간에 있으면 그대로 두기
			} else {
				// 문장 끝에 있으면 제거
				String jsonPart = cleaned.substring(startIdx, endIdx);
				cleaned = cleaned.replace(jsonPart, "").trim();
			}
		}

		return cleaned;
	}

	// 제품 추천 JSON 추출 (개선된 버전)
	private List<ProductSuggestionDTO> extractProductRecommendations(String response) {
		List<ProductSuggestionDTO> suggestions = new ArrayList<>();
		try {
			// 태그 기반 추출 먼저 시도
			if (response.contains("<product_recommendation>") && response.contains("</product_recommendation>")) {
				int startIdx = response.indexOf("<product_recommendation>") + "<product_recommendation>".length();
				int endIdx = response.indexOf("</product_recommendation>");
				String jsonContent = response.substring(startIdx, endIdx).trim();

				// JSON 배열인 경우
				if (jsonContent.trim().startsWith("[")) {
					JSONArray jsonArray = new JSONArray(jsonContent);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject json = jsonArray.getJSONObject(i);
						suggestions.add(parseProductSuggestion(json));
					}
				}
				// 단일 JSON 객체인 경우
				else if (jsonContent.trim().startsWith("{")) {
					JSONObject json = new JSONObject(jsonContent);
					suggestions.add(parseProductSuggestion(json));
				}
				return suggestions;
			}

			// 기존 로직: 태그 없이 JSON 기반 추출
			if (response.contains("{") && response.contains("}")) {
				int startIdx = response.indexOf("{");
				int endIdx = response.lastIndexOf("}") + 1;
				String jsonPart = response.substring(startIdx, endIdx);

				// JSON 배열인 경우
				if (jsonPart.trim().startsWith("[")) {
					JSONArray jsonArray = new JSONArray(jsonPart);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject json = jsonArray.getJSONObject(i);
						suggestions.add(parseProductSuggestion(json));
					}
				}
				// 단일 JSON 객체인 경우
				else {
					JSONObject json = new JSONObject(jsonPart);
					suggestions.add(parseProductSuggestion(json));
				}
			}
			// JSON이 아닌 일반 텍스트 형식의 추천인 경우
			else if (response.contains("제품 추천:") || response.contains("추천 제품:")) {
				String[] lines = response.split("\n");
				for (String line : lines) {
					if (line.contains("제품 추천:") || line.contains("추천 제품:") || line.contains("-")) {
						String productInfo = line.replaceAll("^[\\-\\*]\\s*", "")
							.replace("제품 추천:", "")
							.replace("추천 제품:", "").trim();

						if (!productInfo.isEmpty()) {
							String[] parts = productInfo.split(":");
							String productName = parts[0].trim();
							String reason = parts.length > 1 ? parts[1].trim() : "";

							suggestions.add(ProductSuggestionDTO.builder()
								.productName(productName)
								.reason(reason)
								.build());
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error parsing product recommendations: {}", e.getMessage(), e);
		}
		return suggestions;
	}

	// JSON 객체에서 ProductSuggestionDTO 변환
	private ProductSuggestionDTO parseProductSuggestion(JSONObject json) {
		return ProductSuggestionDTO.builder()
			.productCode(json.optString("productCode", ""))
			.productName(json.optString("productName", json.optString("name", "")))
			.averageRating(json.optDouble("averageRating", json.optDouble("rating", 0.0)))
			.price(json.optInt("price", 0))
			.category(json.optString("category", ""))
			.description(json.optString("description", ""))
			.image(json.optString("image", ""))
			.reason(json.optString("reason", ""))
			.build();
	}
}