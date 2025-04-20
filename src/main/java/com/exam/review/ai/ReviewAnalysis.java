package com.exam.review.ai;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "reviewanalysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAnalysis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "analysisId")
	private Long analysisId;

	@Column(name = "productCode", nullable = false)
	private String productCode;

	@Column(name = "productName")
	private String productName;

	@Column(name = "averageRating")
	private double averageRating;

	@Column(name = "reviewCount")
	private int reviewCount;

	@Column(name = "sentimentPositive")
	private Double sentimentPositive;

	@Column(name = "sentimentNegative")
	private Double sentimentNegative;

	@Column(name = "sentimentNeutral")
	private Double sentimentNeutral;

	@Column(name = "keyPositivePoints", columnDefinition = "JSON")
	private String keyPositivePointsJson;

	@Column(name = "keyNegativePoints", columnDefinition = "JSON")
	private String keyNegativePointsJson;

	@Column(name = "reviewCategories", columnDefinition = "JSON")
	private String reviewCategoriesJson;

	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Column(name = "recommendations", columnDefinition = "JSON")
	private String recommendationsJson;

	@Column(name = "analysisDate")
	private LocalDateTime createdAt;

	// 증분 분석을 위한 필드 추가

	// 이 분석에 포함된 마지막 리뷰 ID (증분 분석 시 기준점)
	@Column(name = "lastReviewId")
	private Long lastReviewId;

	// 이 분석이 증분 분석인지 여부
	@Column(name = "isIncremental")
	private Boolean isIncremental;

	// 분석 시 사용된 샘플 리뷰 수 (대용량 데이터의 경우 샘플링 가능)
	@Column(name = "sampleSize")
	private Integer sampleSize;

	// 분석 유형 (FULL, INCREMENTAL, SAMPLED 등)
	@Column(name = "analysisType")
	private String analysisType;

	// 마지막 업데이트 시간
	@Column(name = "updatedAt")
	private LocalDateTime updatedAt;
}