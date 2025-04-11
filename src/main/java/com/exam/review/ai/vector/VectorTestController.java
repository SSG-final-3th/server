package com.exam.review.ai.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vector-test")
@RequiredArgsConstructor
@Slf4j
public class VectorTestController {

	private final ReviewAnalysisVectorService vectorService;
	private final ReviewAnalysisVectorRepository reviewAnalysisVectorRepository;

	@GetMapping("/save-test-detailed")
	public ResponseEntity<Map<String, Object>> testSaveVectorDetailed() {
		Map<String, Object> response = new HashMap<>();

		try {
			// 1. 테스트 데이터 준비
			int testId = (int)(Math.random() * 1000000);
			String productCode = "TEST-" + testId;
			String reviewText = "테스트 리뷰 텍스트입니다.";
			String summary = "테스트 요약입니다.";
			List<String> positivePoints = Arrays.asList("장점1", "장점2");
			List<String> negativePoints = Arrays.asList("단점1");

			response.put("step1_prepare", "성공: 테스트 데이터 준비 완료");

			// 2. 임베딩 생성 테스트
			float[] testEmbedding = vectorService.createEmbedding("테스트");
			response.put("step2_embedding", "성공: 임베딩 생성 완료 (" + testEmbedding.length + " 차원)");

			// 3. 벡터 포맷팅 테스트
			String vectorStr = vectorService.formatVector(testEmbedding);
			response.put("step3_formatting", "성공: 벡터 포맷팅 완료 (길이: " + vectorStr.length() + ")");

			// 4. ReviewAnalysisVector 객체 생성
			ReviewAnalysisVector vector = ReviewAnalysisVector.builder()
				.analysisId(testId)
				.productCode(productCode)
				.reviewTextEmbeddings(vectorStr)
				.summaryEmbedding(vectorStr)
				.positivePointsEmbedding(vectorStr)
				.negativePointsEmbedding(vectorStr)
				.createdAt(LocalDateTime.now())
				.build();

			response.put("step4_entity", "성공: 엔티티 생성 완료");

			// 5. 리포지토리로 저장 시도
			ReviewAnalysisVector savedVector = reviewAnalysisVectorRepository.save(vector);
			response.put("step5_save", "성공: 벡터 저장 완료 (ID: " + savedVector.getAnalysisId() + ")");

			// 성공 응답
			response.put("success", true);
			response.put("testId", testId);
			response.put("productCode", productCode);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			// 실패한 단계와 오류 메시지 기록
			response.put("success", false);
			response.put("error", e.getMessage());
			response.put("errorType", e.getClass().getName());

			if (e.getCause() != null) {
				response.put("errorCause", e.getCause().getMessage());
			}

			return ResponseEntity.status(500).body(response);
		}
	}
}