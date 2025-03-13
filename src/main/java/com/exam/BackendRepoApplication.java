package com.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class BackendRepoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendRepoApplication.class, args);
		System.out.println("시작");
	}

	/*
	   Cosrs 설정하지 않으면 다음과 같은 에러가 발생됨.
	   Access to XMLHttpRequest at 'http://localhost:8090/app/요청맵핑'
	   from origin 'http://localhost:3000' has been blocked by CORS policy:
	   Response to preflight request doesn't pass access control
	   check: No 'Access-Control-Allow-Origin' header is present
	   on the requested resource.

	*/
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
