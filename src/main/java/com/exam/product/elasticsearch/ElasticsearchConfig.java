package com.exam.product.elasticsearch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

	@Value("${elasticsearch.host}")
	private String openSearchHost;

	public String getOpenSearchHost() {
		return openSearchHost;
	}
}
