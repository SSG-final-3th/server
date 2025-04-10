package com.exam.review.ai.vector;

import java.util.List;

import javax.naming.directory.SearchResult;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewAnalysisVectorRepository extends JpaRepository<ReviewAnalysisVector, Integer> {

	// 요약 기반 유사 분석 검색 (native query 사용)
	@Query(value =
		"SELECT v.analysis_id, v.product_code, " +
			"1 - (v.summary_embedding::vector <=> :embedding\\:\\:vector) as similarity " +
			"FROM review_analysis_vector v " +
			"ORDER BY similarity DESC " +
			"LIMIT :limit",
		nativeQuery = true)
	List<Object[]> findSimilarByEmbedding(@Param("embedding") String embedding, @Param("limit") int limit);

	// 긍정 포인트 기반 유사 검색
	@Query(value =
		"SELECT v.analysis_id, v.product_code, " +
			"1 - (v.positive_points_embedding::vector <=> :embedding\\:\\:vector) as similarity " +
			"FROM review_analysis_vector v " +
			"ORDER BY similarity DESC " +
			"LIMIT :limit",
		nativeQuery = true)
	List<Object[]> findSimilarByPositiveEmbedding(@Param("embedding") String embedding, @Param("limit") int limit);

	// 부정 포인트 기반 유사 검색
	@Query(value =
		"SELECT v.analysis_id, v.product_code, " +
			"1 - (v.negative_points_embedding::vector <=> :embedding\\:\\:vector) as similarity " +
			"FROM review_analysis_vector v " +
			"ORDER BY similarity DESC " +
			"LIMIT :limit",
		nativeQuery = true)
	List<Object[]> findSimilarByNegativeEmbedding(@Param("embedding") String embedding, @Param("limit") int limit);

}