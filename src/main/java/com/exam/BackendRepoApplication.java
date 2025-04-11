package com.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // 👈 추가!

import lombok.extern.slf4j.Slf4j;

@EnableAsync // 비동기 처리를 위한 어노테이션(반드시 메인 어플리케이션에)
@EnableScheduling // 재고 감시를 위한 스케쥴링 어노테이션 추가
@SpringBootApplication
@EnableJpaAuditing  // 👈 여기 추가!
@Slf4j
public class BackendRepoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendRepoApplication.class, args);
		System.out.println("시작");
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		log.info("WebMvcConfigurer.addCorsMappings");
		return new WebMvcConfigurer() {
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
					.allowedMethods("*")
					.allowedOrigins("http://localhost:3000", "*");
			}
		};
	}
}
