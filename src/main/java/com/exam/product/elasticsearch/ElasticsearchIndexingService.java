package com.exam.product.elasticsearch;

import com.exam.product.Product;
import com.exam.product.ProductDTO;
import com.exam.product.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexingService {

	private final ProductRepository productRepository;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final ElasticsearchConfig elasticsearchConfig;

	@Scheduled(cron = "0 0 * * * *")  // 매 시간 정각에 실행
	public void indexAllProducts() {
		log.info("🔄 모든 상품 인덱싱 시작");

		try {
			// ✅ 전체 상품 조회
			List<Product> products = productRepository.findAll();

			// ✅ Product → ProductDTO 수동 변환
			List<ProductDTO> productDTOs = products.stream()
				.map(p -> new ProductDTO(
					p.getProductCode(),
					p.getProductName(),
					p.getCategory(),
					p.getPrice()
				))
				.collect(Collectors.toList());

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
						log.error("❌ JSON 변환 실패", e);
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

			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("✅ 상품 인덱싱 성공");
			} else {
				log.error("❌ 상품 인덱싱 실패: {}", response.getBody());
			}

		} catch (Exception e) {
			log.error("❗ 인덱싱 도중 에러 발생", e);
		}
	}
}
