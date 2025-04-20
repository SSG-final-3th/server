package com.exam.product.elasticsearch;

import com.exam.product.Product;
import com.exam.product.ProductDTO;
import com.exam.product.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexingService {
	private final ProductRepository productRepository;
	private final JdbcTemplate jdbcTemplate;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final ElasticsearchConfig elasticsearchConfig;

	// 마지막 전체 인덱싱 시간을 저장하는 변수
	private LocalDateTime lastFullIndexingTime = LocalDateTime.now().minusYears(1);

	/**
	 * 전체 상품 데이터 인덱싱 - 주 1회만 실행 (일요일 새벽 2시)
	 */
	@Scheduled(cron = "0 0 2 * * SUN")
	public void indexAllProducts() {
		log.info("🔄 주간 전체 상품 인덱싱 시작");
		try {
			// ✅ 전체 상품 조회
			List<Product> products = productRepository.findAll();

			// ✅ Product → ProductDTO 수동 변환 및 인덱싱
			indexProductsToElasticsearch(convertToProductDTOs(products));

			// ✅ 검색 키워드 인덱싱
			indexSearchKeywords();

			// 마지막 전체 인덱싱 시간 업데이트
			updateLastFullIndexingTime(LocalDateTime.now());
			log.info("✅ 전체 상품 및 검색 키워드 인덱싱 완료 - 총 {}개 상품", products.size());
		} catch (Exception e) {
			log.error("❗ 전체 인덱싱 도중 에러 발생", e);
		}
	}

	/**
	 * 증분 인덱싱: 마지막 인덱싱 이후 변경된 데이터만 인덱싱
	 * 매일 새벽 3시에 실행
	 */
	@Scheduled(cron = "0 0 3 * * *")
	public void indexChangedData() {
		log.info("🔄 변경된 데이터 인덱싱 시작 - 마지막 인덱싱: {}", lastFullIndexingTime);
		try {
			// ✅ 1. 변경된 상품 인덱싱
			indexChangedProducts();

			// ✅ 2. 변경된 검색 키워드 인덱싱
			indexChangedSearchKeywords();

			log.info("✅ 변경된 데이터 인덱싱 완료");
		} catch (Exception e) {
			log.error("❗ 증분 인덱싱 도중 에러 발생", e);
		}
	}

	/**
	 * 변경된 상품만 인덱싱
	 */
	private void indexChangedProducts() {
		try {
			// ✅ 마지막 인덱싱 이후 변경된 상품 조회
			String sql = "SELECT p.* FROM products p WHERE p.updatedAt > ?";
			List<Map<String, Object>> changedProducts = jdbcTemplate.queryForList(sql, lastFullIndexingTime);

			if (changedProducts.isEmpty()) {
				log.info("✅ 변경된 상품이 없습니다");
				return;
			}

			// ✅ Map → ProductDTO 변환
			List<ProductDTO> productDTOs = changedProducts.stream()
				.map(p -> new ProductDTO(
					(String) p.get("productCode"),
					(String) p.get("productName"),
					(String) p.get("category"),
					((Number) p.get("price")).intValue()
				))
				.collect(Collectors.toList());

			// ✅ 변경된 상품 인덱싱
			indexProductsToElasticsearch(productDTOs);
			log.info("✅ 변경된 상품 인덱싱 완료 - 총 {}개 상품", changedProducts.size());
		} catch (Exception e) {
			log.error("❗ 변경된 상품 인덱싱 중 에러 발생", e);
		}
	}

	/**
	 * 변경된 검색 키워드 인덱싱
	 */
	private void indexChangedSearchKeywords() {
		try {
			// ✅ 마지막 인덱싱 이후 변경된 검색 키워드 조회
			String sql =
				"SELECT k.keywordId, k.keyword, k.searchCount, " +
					"GROUP_CONCAT(DISTINCT m.productCode) as productCodes, " +
					"SUM(m.clickCount) as totalClicks " +
					"FROM searchkeywords k " +
					"LEFT JOIN keywordproductmapping m ON k.keywordId = m.keywordId " +
					"WHERE k.updatedAt > ? OR m.updatedAt > ? " +
					"GROUP BY k.keywordId, k.keyword, k.searchCount";

			List<Map<String, Object>> changedKeywords = jdbcTemplate.queryForList(sql,
				lastFullIndexingTime, lastFullIndexingTime);

			if (changedKeywords.isEmpty()) {
				log.info("✅ 변경된 검색 키워드가 없습니다");
				return;
			}

			// ✅ 검색 키워드 인덱싱
			indexKeywordsToElasticsearch(changedKeywords);
			log.info("✅ 변경된 검색 키워드 인덱싱 완료 - 총 {}개 키워드", changedKeywords.size());
		} catch (Exception e) {
			log.error("❗ 변경된 검색 키워드 인덱싱 중 에러 발생", e);
		}
	}

	/**
	 * 모든 검색 키워드 인덱싱
	 */
	private void indexSearchKeywords() {
		try {
			// ✅ 모든 검색 키워드와 관련 상품 조회
			String sql =
				"SELECT k.keywordId, k.keyword, k.searchCount, " +
					"GROUP_CONCAT(DISTINCT m.productCode) as productCodes, " +
					"SUM(m.clickCount) as totalClicks " +
					"FROM searchkeywords k " +
					"LEFT JOIN keywordproductmapping m ON k.keywordId = m.keywordId " +
					"GROUP BY k.keywordId, k.keyword, k.searchCount";

			List<Map<String, Object>> keywords = jdbcTemplate.queryForList(sql);

			if (keywords.isEmpty()) {
				log.info("✅ 인덱싱할 검색 키워드가 없습니다");
				return;
			}

			// ✅ 검색 키워드 인덱싱
			indexKeywordsToElasticsearch(keywords);
			log.info("✅ 검색 키워드 인덱싱 완료 - 총 {}개 키워드", keywords.size());
		} catch (Exception e) {
			log.error("❗ 검색 키워드 인덱싱 중 에러 발생", e);
		}
	}

	/**
	 * 검색 키워드를 Elasticsearch에 인덱싱
	 */
	private void indexKeywordsToElasticsearch(List<Map<String, Object>> keywords) throws Exception {
		// ✅ Bulk 요청 형식으로 변환
		String bulkBody = keywords.stream()
			.map(keyword -> {
				try {
					Long keywordId = ((Number) keyword.get("keywordId")).longValue();
					String meta = objectMapper.writeValueAsString(Map.of(
						"index", Map.of("_index", "search_keywords", "_id", keywordId)
					));

					// 관련 상품 목록 처리
					String productCodesStr = (String) keyword.get("productCodes");
					String[] productCodes = productCodesStr != null ?
						productCodesStr.split(",") :
						new String[0];

					Map<String, Object> data = new HashMap<>();
					data.put("keyword_id", keywordId);
					data.put("keyword", keyword.get("keyword"));
					data.put("search_count", ((Number) keyword.get("searchCount")).intValue());
					data.put("product_codes", productCodes);
					data.put("total_clicks", keyword.get("totalClicks") != null ?
						((Number) keyword.get("totalClicks")).intValue() : 0);
					data.put("updated_at", LocalDateTime.now());

					String dataJson = objectMapper.writeValueAsString(data);
					return meta + "\n" + dataJson;
				} catch (Exception e) {
					log.error("❌ 키워드 JSON 변환 실패", e);
					return "";
				}
			})
			.collect(Collectors.joining("\n")) + "\n";

		// ✅ Elasticsearch에 bulk 요청
		String endpoint = elasticsearchConfig.getOpenSearchHost() + "/_bulk";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(bulkBody, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			log.error("❌ 키워드 인덱싱 실패: {}", response.getBody());
		}
	}

	/**
	 * Product 목록을 ProductDTO로 변환
	 */
	private List<ProductDTO> convertToProductDTOs(List<Product> products) {
		return products.stream()
			.map(p -> new ProductDTO(
				p.getProductCode(),
				p.getProductName(),
				p.getCategory(),
				p.getPrice()
			))
			.collect(Collectors.toList());
	}

	/**
	 * Elasticsearch에 상품 목록을 인덱싱하는 공통 메서드
	 */
	private void indexProductsToElasticsearch(List<ProductDTO> productDTOs) throws Exception {
		if (productDTOs.isEmpty()) {
			log.info("✅ 인덱싱할 상품이 없습니다");
			return;
		}

		// ✅ Bulk 요청 형식으로 변환
		String bulkBody = productDTOs.stream()
			.map(product -> {
				try {
					String meta = objectMapper.writeValueAsString(Map.of(
						"index", Map.of("_index", "products", "_id", product.getProductCode())
					));
					String data = objectMapper.writeValueAsString(product);
					return meta + "\n" + data;
				} catch (Exception e) {
					log.error("❌ 상품 JSON 변환 실패", e);
					return "";
				}
			})
			.collect(Collectors.joining("\n")) + "\n";

		// ✅ Elasticsearch에 bulk 요청
		String endpoint = elasticsearchConfig.getOpenSearchHost() + "/_bulk";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(bulkBody, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			log.error("❌ 상품 인덱싱 실패: {}", response.getBody());
		}
	}

	// 마지막 전체 인덱싱 시간 업데이트
	private void updateLastFullIndexingTime(LocalDateTime time) {
		this.lastFullIndexingTime = time;
		log.info("📅 마지막 전체 인덱싱 시간 업데이트: {}", time);
		// 필요시 DB나 파일에 저장하는 로직 추가
	}

	// 수동 인덱싱을 위한 API 엔드포인트에서 호출할 수 있는 메소드
	public void runManualFullIndexing() {
		log.info("🔄 수동 전체 인덱싱 시작");
		indexAllProducts();
	}
}