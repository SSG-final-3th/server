package com.exam.product.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSearchService {

	private final ElasticsearchIndexService elasticsearchIndexService;
	private final ElasticsearchClient esClient;

	// 한국어 검색어와 영어 카테고리 매핑
	private String mapKeywordToCategory(String keyword) {
		// 한국어 검색어를 영어 카테고리로 매핑
		Map<String, String> keywordMap = new HashMap<>();
		keywordMap.put("고기", "meat");
		keywordMap.put("육류", "meat");
		keywordMap.put("해산물", "seafood");
		keywordMap.put("유제품", "dairy");
		keywordMap.put("음료", "beverage");
		keywordMap.put("채소", "vegetable");
		keywordMap.put("과일", "fruit");
		keywordMap.put("간식", "snack");
		keywordMap.put("조미료", "condiment");
		keywordMap.put("건강식품", "healthfood");
		keywordMap.put("면", "noodle");
		keywordMap.put("밥", "rice");

		return keywordMap.getOrDefault(keyword, keyword);
	}

	/**
	 * 제품 키워드 검색
	 */
	public List<Map<String, Object>> searchProducts(String keyword, int limit) {
		try {
			elasticsearchIndexService.initializeIfNeeded();
			// 검색어가 비어있는 경우 처리
			if (keyword == null || keyword.trim().isEmpty()) {
				log.warn("빈 검색어로 검색 시도");
				return new ArrayList<>();
			}

			// 최소 글자 수 검증
			String trimmedKeyword = keyword.trim();
			if (trimmedKeyword.length() < 2) {
				log.info("검색어가 너무 짧습니다: {}", trimmedKeyword);
				// 최소 2글자 이상 입력하도록 유도하거나 기본 검색 수행
			}

			// 특수 문자 제거 또는 이스케이프 처리
			String sanitizedKeyword = sanitizeSearchTerm(trimmedKeyword);
			String mappedCategory = mapKeywordToCategory(sanitizedKeyword);

			log.info("검색어: {}, 매핑된 카테고리: {}", sanitizedKeyword, mappedCategory);

			// 검색 쿼리 구성 - bool 쿼리로 변경하여 원래 키워드와 매핑된 카테고리 검색
			Query query = Query.of(q -> q
				.bool(b -> b
					.should(s -> s
						.multiMatch(m -> m
							.query(sanitizedKeyword)
							.fields("product_name^3", "description")
							.fuzziness("AUTO")
						)
					)
					.should(s -> s
						.match(m -> m
							.field("category")
							.query(mappedCategory)
							.boost(2.0f)
						)
					)
					.minimumShouldMatch("1")
				)
			);

			SearchRequest request = SearchRequest.of(r -> r
				.index("products")
				.query(query)
				.size(limit)
			);

			// 검색 실행
			SearchResponse<Map> response = esClient.search(request, Map.class);

			// 결과가 없는 경우
			if (response.hits().hits().isEmpty()) {
				log.info("검색어 '{}', 매핑된 카테고리 '{}': 결과 없음", sanitizedKeyword, mappedCategory);
				return new ArrayList<>();
			}

			return convertToSearchResults(response);
		} catch (Exception e) {
			log.error("엘라스틱서치 제품 검색 중 오류: {}", e.getMessage(), e);
			// 오류 발생 시 빈 결과 반환 또는 기본 결과 제공
			return new ArrayList<>();
		}
	}

	/**
	 * 오탈자 처리 또는 자동 교정 검색
	 */
	public Map<String, Object> searchWithSuggestion(String keyword, int limit) {
		Map<String, Object> result = new HashMap<>();

		try {
			// 일반 검색 실행
			List<Map<String, Object>> searchResults = searchProducts(keyword, limit);
			result.put("results", searchResults);

			// 결과가 없다면 교정 제안
			if (searchResults.isEmpty()) {
				// 제안 쿼리 구성
				Query suggestionQuery = Query.of(q -> q
					.matchPhrasePrefix(m -> m
						.field("product_name")
						.query(keyword)
						.maxExpansions(10)
					)
				);

				SearchRequest suggestionRequest = SearchRequest.of(r -> r
					.index("products")
					.query(suggestionQuery)
					.size(3) // 상위 제안만 가져오기
				);

				SearchResponse<Map> suggestionResponse = esClient.search(suggestionRequest, Map.class);

				List<String> suggestions = new ArrayList<>();
				for (Hit<Map> hit : suggestionResponse.hits().hits()) {
					Map<String, Object> source = hit.source();
					if (source != null && source.containsKey("product_name")) {
						suggestions.add((String) source.get("product_name"));
					}
				}

				if (!suggestions.isEmpty()) {
					result.put("suggestions", suggestions);
					log.info("검색어 '{}': 제안 제공 - {}", keyword, suggestions);
				}
			}

			return result;
		} catch (Exception e) {
			log.error("제안 검색 중 오류: {}", e.getMessage(), e);
			result.put("results", new ArrayList<>());
			return result;
		}
	}

	/**
	 * 검색어 정제 - 특수문자 제거 및 안전한 검색어로 변환
	 */
	private String sanitizeSearchTerm(String keyword) {
		// 기본적인 특수문자 제거 또는 이스케이프 처리
		String sanitized = keyword
			.replaceAll("[\\\\+\\-=&|><!(){}\\[\\]^\"~*?:/]", " ")
			.replaceAll("\\s+", " ")
			.trim();

		return sanitized;
	}

	/**
	 * 검색 응답을 표준 형식으로 변환
	 */
	private List<Map<String, Object>> convertToSearchResults(SearchResponse<Map> response) {
		List<Map<String, Object>> results = new ArrayList<>();
		int rank = 1;

		for (Hit<Map> hit : response.hits().hits()) {
			Map<String, Object> source = hit.source();
			if (source == null) {
				log.warn("검색 결과에 소스 데이터가 없습니다: {}", hit.id());
				continue;
			}

			Map<String, Object> item = new HashMap<>();

			item.put("rank", rank++);
			item.put("product_code", source.getOrDefault("product_code", "unknown"));
			item.put("product_name", source.getOrDefault("product_name", "제품명 없음"));
			item.put("score", hit.score());

			// 임의의 순위 변동 (실제로는 이전 검색 결과와 비교해야 함)
			double randomChange = Math.random() * 6 - 3; // -3 ~ +3
			item.put("rankChange", (int)randomChange);

			results.add(item);
		}

		return results;
	}

	public ElasticsearchClient getElasticsearchClient() {
		return this.esClient;
	}
}