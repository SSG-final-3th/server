package com.exam.review.ai.vector;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_analysis_vector")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAnalysisVector {

	@Id
	@Column(name = "analysis_id")
	private Integer analysisId;

	@Column(name = "product_code", nullable = false)
	private String productCode;

	// 벡터는 String으로 저장하고 네이티브 쿼리에서 캐스팅
	@Column(name = "review_text_embeddings", columnDefinition = "text")
	private String reviewTextEmbeddings;

	@Column(name = "summary_embedding", columnDefinition = "text")
	private String summaryEmbedding;

	@Column(name = "positive_points_embedding", columnDefinition = "text")
	private String positivePointsEmbedding;

	@Column(name = "negative_points_embedding", columnDefinition = "text")
	private String negativePointsEmbedding;

	// 생성 시간 필드 추가
	@Column(name = "created_at")
	private LocalDateTime createdAt;
}