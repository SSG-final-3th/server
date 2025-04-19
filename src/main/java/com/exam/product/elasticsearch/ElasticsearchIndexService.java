package com.exam.product.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexService {

	private final ElasticsearchClient esClient;

	private static boolean initialized = false;

	public synchronized void initializeIfNeeded() {
		if (!initialized) {
			int maxAttempts = 10;
			int waitSeconds = 3;
			for (int attempt = 1; attempt <= maxAttempts; attempt++) {
				try {
					createProductIndex();
					initialized = true;
					log.info("Elasticsearch 인덱스 초기화 성공 (시도 {}회)", attempt);
					return;
				} catch (Exception e) {
					log.warn("Elasticsearch 인덱스 초기화 실패 (시도 {}회): {}", attempt, e.getMessage());
					try {
						Thread.sleep(waitSeconds * 1000L);
					} catch (InterruptedException ignored) {}
				}
			}
			log.error("Elasticsearch 인덱스 초기화에 반복적으로 실패했습니다.");
		}
	}


	private void createProductIndex() throws IOException {
		String indexName = "products";

		boolean exists = esClient.indices().exists(
			new ExistsRequest.Builder().index(indexName).build()
		).value();

		if (!exists) {
			// 인덱스 생성 - category를 text 타입으로 설정
			CreateIndexResponse response = esClient.indices().create(c -> c
				.index(indexName)
				.mappings(m -> m
					.properties("product_code", p -> p.keyword(k -> k))
					.properties("product_name", p -> p.text(t -> t
						.analyzer("standard")
						.fields("keyword", f -> f.keyword(k -> k))
					))
					// category를 text 타입으로 변경하여 검색 가능하게 함
					.properties("category", p -> p.text(t -> t
						.analyzer("standard")
					))
					.properties("description", p -> p.text(t -> t.analyzer("standard")))
					.properties("price", p -> p.integer(i -> i))
					.properties("image", p -> p.keyword(k -> k))
				)
			);

			log.info("제품 인덱스 생성 결과: {}", response.acknowledged());
		} else {
			log.info("제품 인덱스가 이미 존재합니다.");
		}
	}
}