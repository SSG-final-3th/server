package com.exam.review.ai;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAnalysisResponseDTO {
	private String productCode;
	private String productName;
	private double averageRating;
	private int reviewCount;

	private Map<String, Double> sentimentAnalysis;
	private List<String> keyPositivePoints;
	private List<String> keyNegativePoints;
	// 리뷰 카테고리
	private List<ReviewCategoryDTO> reviewCategories;
	// 요약
	private String summary;
	private List<String> recommendations;

}