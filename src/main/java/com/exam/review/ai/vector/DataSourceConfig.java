package com.exam.review.ai.vector;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

	//필터로 리파지토리 에서 MySql은 전체 패키지를 스캔 벡터 파일은 벡터 패키지만 설정하게 하는 부분
	@Configuration
	@EnableJpaRepositories(
		basePackages = "com.exam", // 모든 com.exam 하위 패키지를 스캔
		excludeFilters = @ComponentScan.Filter(
			type = FilterType.REGEX,
			pattern = "com\\.exam\\.review\\.ai\\.vector\\..*" // 벡터 관련 패키지만 제외
		),
		entityManagerFactoryRef = "entityManagerFactory",
		transactionManagerRef = "transactionManager"
	)
	public class MainDataSourceConfig {
		// 기본 MySQL 데이터소스 설정
		@Primary
		@Bean(name = "dataSource")
		@ConfigurationProperties("spring.datasource.mysql")
		public DataSource dataSource() {
			return DataSourceBuilder.create().build();
		}
		//MySQL
		@Primary
		@Bean(name = "entityManagerFactory")
		public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			EntityManagerFactoryBuilder builder, @Qualifier("dataSource") DataSource dataSource) {
			return builder
				.dataSource(dataSource)
				.packages(
					"com.exam.admin",
					"com.exam.adminbranch",
					"com.exam.answer",
					"com.exam.Cart",
					"com.exam.category",
					"com.exam.config",
					"com.exam.coupon",
					"com.exam.exception",
					"com.exam.event",
					"com.exam.inventory",
					"com.exam.mypage",
					"com.exam.offline",
					"com.exam.orderinfo",
					"com.exam.product",
					"com.exam.product.elasticsearch",
					"com.exam.quartz",
					"com.exam.question",
					"com.exam.review",
					"com.exam.security",
					"com.exam.social",
					"com.exam.user",
					"com.exam.search.keyword"
					// "com.exam.review.ai.vector"는 제외
				)
				.persistenceUnit("main")
				.build();
		}

		@Primary
		@Bean(name = "transactionManager")
		public PlatformTransactionManager transactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
			return new JpaTransactionManager(entityManagerFactory);
		}
	}

	@Configuration
	@EnableJpaRepositories(
		basePackages = "com.exam.review.ai.vector",
		entityManagerFactoryRef = "vectorEntityManagerFactory",
		transactionManagerRef = "vectorTransactionManager"
	)
	public class VectorDataSourceConfig {
		// PostgreSQL 벡터 데이터소스 설정
		@Bean(name = "vectorDataSource")
		@ConfigurationProperties("spring.datasource.postgres")
		public DataSource vectorDataSource() {
			return DataSourceBuilder.create().build();
		}

		@Bean(name = "vectorEntityManagerFactory")
		public LocalContainerEntityManagerFactoryBean vectorEntityManagerFactory(
			EntityManagerFactoryBuilder builder, @Qualifier("vectorDataSource") DataSource dataSource) {

			Map<String, String> properties = new HashMap<>();
			properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

			return builder
				.dataSource(dataSource)
				.packages("com.exam.review.ai.vector")
				.properties(properties)
				.persistenceUnit("vector")
				.build();
		}

		@Bean(name = "vectorTransactionManager")
		public PlatformTransactionManager vectorTransactionManager(
			@Qualifier("vectorEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
			return new JpaTransactionManager(entityManagerFactory);
		}
	}

	@Bean(name = "vectorJdbcTemplate")
	public JdbcTemplate vectorJdbcTemplate(@Qualifier("vectorDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}