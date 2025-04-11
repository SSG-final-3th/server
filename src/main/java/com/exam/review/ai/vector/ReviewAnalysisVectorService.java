package com.exam.review.ai.vector;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAnalysisVectorService {

	private final ReviewAnalysisVectorRepository vectorRepository;
	private final RestTemplate restTemplate;

	@Autowired
	@Qualifier("vectorJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Value("${spring.ai.openai.api-key}")
	private String openaiApiKey;

	@Value("${spring.ai.openai.embeddings.url:https://api.openai.com/v1/embeddings}")
	private String openaiApiUrl;

	// 텍스트에서 임베딩 생성
	public float[] createEmbedding(String text) {
		log.info("Creating embedding for text (첫 50자): {}",
			text.length() > 50 ? text.substring(0, 50) + "..." : text);

		// 헤더 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(openaiApiKey);  // API 키로 인증 헤더 설정

		// 요청 본문 구성
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", "text-embedding-3-small");
		requestBody.put("input", text);

		// HTTP 엔티티 생성
		HttpEntity<Map<String, Object>> entity =
			new HttpEntity<>(requestBody, headers);

		try {
			// exchange 메서드 사용
			log.info("OpenAI API 호출: {}", openaiApiUrl);
			ResponseEntity<Map> response = restTemplate.exchange(
				openaiApiUrl,
				HttpMethod.POST,
				entity,
				Map.class
			);

			log.info("OpenAI API 응답 상태: {}", response.getStatusCode());

			Map<String, Object> responseBody = response.getBody();
			if (responseBody == null) {
				throw new RuntimeException("OpenAI API에서 빈 응답이 반환되었습니다");
			}

			List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
			if (data == null || data.isEmpty()) {
				log.error("응답에 data 필드가 없습니다: {}", responseBody);
				throw new RuntimeException("임베딩 데이터가 없습니다");
			}

			// Double로 변경
			List<Double> embedding = (List<Double>) data.get(0).get("embedding");

			// List<Double>를 float[]로 변환
			float[] embeddingArray = new float[embedding.size()];
			for (int i = 0; i < embedding.size(); i++) {
				embeddingArray[i] = embedding.get(i).floatValue(); // Double을 float로 명시적 변환
			}

			log.info("임베딩 생성 완료: {} 차원", embeddingArray.length);
			return embeddingArray;
		} catch (Exception e) {
			log.error("임베딩 생성 중 오류: {}", e.getMessage(), e);
			throw new RuntimeException("임베딩 생성 실패: " + e.getMessage(), e);
		}
	}

	// 리뷰 분석 벡터 저장
	@Transactional("vectorTransactionManager")
	public void saveReviewAnalysisVector(int analysisId,
		String productCode,
		String reviewsText,
		String summary,
		List<String> positivePoints,
		List<String> negativePoints) {

		try {
			log.info("벡터 저장 시작 - 분석 ID: {}, 상품 코드: {}", analysisId, productCode);

			// 리뷰 텍스트 임베딩
			float[] reviewTextEmbedding = createEmbedding(reviewsText);
			log.info("리뷰 텍스트 임베딩 생성 완료: {} 차원", reviewTextEmbedding.length);

			// 요약 임베딩
			float[] summaryEmbedding = createEmbedding(summary);
			log.info("요약 임베딩 생성 완료: {} 차원", summaryEmbedding.length);

			// 긍정적 포인트 임베딩 (모든 포인트를 결합)
			String positiveText = String.join(" ", positivePoints);
			float[] positiveEmbedding = positiveText.isEmpty() ?
				new float[0] :
				createEmbedding(positiveText);
			log.info("긍정 포인트 임베딩 생성 완료: {} 차원", positiveEmbedding.length);

			// 부정적 포인트 임베딩 (모든 포인트를 결합)
			String negativeText = String.join(" ", negativePoints);
			float[] negativeEmbedding = negativeText.isEmpty() ?
				new float[0] :
				createEmbedding(negativeText);
			log.info("부정 포인트 임베딩 생성 완료: {} 차원", negativeEmbedding.length);

			// 벡터 포맷팅
			String reviewTextEmbeddingsStr = formatVector(reviewTextEmbedding);
			String summaryEmbeddingStr = formatVector(summaryEmbedding);
			String positivePointsEmbeddingStr = formatVector(positiveEmbedding);
			String negativePointsEmbeddingStr = formatVector(negativeEmbedding);

			// 네이티브 SQL로 직접 삽입 (::vector 캐스팅 포함)
			String sql = "INSERT INTO review_analysis_vector " +
				"(analysis_id, product_code, review_text_embeddings, summary_embedding, " +
				"positive_points_embedding, negative_points_embedding, created_at) " +
				"VALUES (?, ?, ?::vector, ?::vector, ?::vector, ?::vector, ?)";

			int rowsAffected = jdbcTemplate.update(sql,
				analysisId,
				productCode,
				reviewTextEmbeddingsStr,
				summaryEmbeddingStr,
				positivePointsEmbeddingStr,
				negativePointsEmbeddingStr,
				Timestamp.valueOf(LocalDateTime.now())
			);

			log.info("벡터 저장 완료 - 분석 ID: {}, 영향받은 행: {}", analysisId, rowsAffected);

		} catch (Exception e) {
			log.error("벡터 저장 중 오류 발생: {}", e.getMessage(), e);
			throw new RuntimeException("벡터 저장 실패: " + e.getMessage(), e);
		}
	}

	// 유사 제품 검색 (요약 기반)
	public List<Map<String, Object>> findSimilarProductsBySummary(String searchText, int limit) {
		try {
			log.info("요약 기반 유사 제품 검색: {}", searchText);
			float[] searchEmbedding = createEmbedding(searchText);
			String vectorStr = formatVector(searchEmbedding);

			List<Object[]> results = vectorRepository.findSimilarByEmbedding(vectorStr, limit);
			log.info("검색 결과: {} 개 항목 발견", results.size());
			return convertToResultMap(results);
		} catch (Exception e) {
			log.error("요약 기반 검색 중 오류: {}", e.getMessage(), e);
			throw new RuntimeException("유사 제품 검색 실패: " + e.getMessage(), e);
		}
	}

	// 긍정적 감정 기반 유사 제품 검색
	public List<Map<String, Object>> findProductsByPositiveEmotion(String emotion, int limit) {
		try {
			log.info("긍정 감정 기반 검색: {}", emotion);
			float[] emotionEmbedding = createEmbedding(emotion);
			String vectorStr = formatVector(emotionEmbedding);

			List<Object[]> results = vectorRepository.findSimilarByPositiveEmbedding(vectorStr, limit);
			log.info("검색 결과: {} 개 항목 발견", results.size());
			return convertToResultMap(results);
		} catch (Exception e) {
			log.error("긍정 감정 기반 검색 중 오류: {}", e.getMessage(), e);
			throw new RuntimeException("긍정 감정 기반 검색 실패: " + e.getMessage(), e);
		}
	}

	// 부정적 감정 기반 유사 제품 검색
	public List<Map<String, Object>> findProductsByNegativeEmotion(String emotion, int limit) {
		try {
			log.info("부정 감정 기반 검색: {}", emotion);
			float[] emotionEmbedding = createEmbedding(emotion);
			String vectorStr = formatVector(emotionEmbedding);

			List<Object[]> results = vectorRepository.findSimilarByNegativeEmbedding(vectorStr, limit);
			log.info("검색 결과: {} 개 항목 발견", results.size());
			return convertToResultMap(results);
		} catch (Exception e) {
			log.error("부정 감정 기반 검색 중 오류: {}", e.getMessage(), e);
			throw new RuntimeException("부정 감정 기반 검색 실패: " + e.getMessage(), e);
		}
	}

	// 결과를 Map으로 변환
	private List<Map<String, Object>> convertToResultMap(List<Object[]> results) {
		return results.stream()
			.map(result -> {
				Map<String, Object> map = new HashMap<>();
				map.put("analysis_id", result[0]);
				map.put("product_code", result[1]);
				map.put("similarity", result[2]);
				return map;
			})
			.collect(Collectors.toList());
	}

	// 벡터 포맷팅 (PostgreSQL에 맞게)
	public String formatVector(float[] embedding) {
		if (embedding == null || embedding.length == 0) {
			return "[]";  // 빈 배열 반환
		}

		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < embedding.length; i++) {
			sb.append(embedding[i]);
			if (i < embedding.length - 1) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}