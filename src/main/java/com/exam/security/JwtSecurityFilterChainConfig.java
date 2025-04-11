package com.exam.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class JwtSecurityFilterChainConfig {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
		HandlerMappingIntrospector handlerMappingIntrospector) throws Exception {

		logger.info("logger: SecurityFilterChainConfig().filterChain:{} ", httpSecurity);

		MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(handlerMappingIntrospector);

		// csrf 비활성화
		httpSecurity
			.csrf(AbstractHttpConfigurer::disable);

		// /home, /signup , /authenticate 3가지 인증없이 요청이 가능
		// 나머지 요청은 모두 인증 필요. 만약에 인증없이 요청하면 401 에러가 발생됨.
		//         httpSecurity.authorizeHttpRequests((authorize) -> authorize
		//                  .requestMatchers(mvcMatcherBuilder.pattern("/home")).permitAll()
		//                 // .requestMatchers("/board").permitAll()
		//                  //.requestMatchers("/hello-world","/signup").permitAll()
		//                  //.requestMatchers(mvcMatcherBuilder.pattern("/authenticate")).permitAll()
		//                  //import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;
		//                  // 클라이언트에서 서버로 요청할 때 미리 서버가 요청을 제대로 받을 수 있는지
		//                  // 시험적으로 요청하는 작업이 있음. 예> ping 작업.  이것을 preflight 작업이라고 한다.
		//                  // 이때 OPTIONS 메서드로 요청함. 따라서 이것을 허용하는 작업임.
		//                  .requestMatchers(mvcMatcherBuilder.pattern(HttpMethod.OPTIONS,"/**")).permitAll()
		//                  .anyRequest()
		//                  .authenticated()
		//                );

		httpSecurity.authorizeHttpRequests((authorize) -> authorize
			// 모든 경로에 대해 인증 없이 접근 허용

			.requestMatchers("/home", "/signup", "/authenticate", "/reset-password", "/findid"
				, "/*", "/cart/**","/questions/**", "/find*","/answers/**", "/**","/login/naver/**").permitAll()

			//.requestMatchers("/**").permitAll()
			// OPTIONS 메서드에 대한 요청도 인증 없이 허용

			//Google 로그인 API 경로 추가
			.requestMatchers("/auth/google", "/offprice/**").permitAll()
			.requestMatchers("/product/**", "/review/**", "/order/**", "/admin/**","/auth/kakao").permitAll()
			.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
			.anyRequest().authenticated()  // 그 외의 요청은 인증 필요
		);

		// 기본 인증 활성화(인증 팝업창 활성화)작업.
		// 기본인증 활성화? 사용자id와 pw를 묻는 웹페이지 대신에 인증하기 위한 팝업이 제공됨을 의미한다.
		httpSecurity
			.httpBasic(Customizer.withDefaults());

		// 4. 상태가 없는 세션을 작성.  CSRF를 비활성화 한다면 반드시 세션에 상태가 없어야 된다. (******)
		httpSecurity
			.sessionManagement(
				session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			);

		httpSecurity
			.oauth2ResourceServer((oauth2) -> oauth2
				.jwt(Customizer.withDefaults())
			);

		return httpSecurity.build();
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		JWKSet jwkSet = new JWKSet(rsaKey());
		return (((jwkSelector, securityContext)
			-> jwkSelector.select(jwkSet)));
	}

	@Bean
	JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwtEncoder(jwkSource);
	}

	@Bean
	JwtDecoder jwtDecoder() throws JOSEException {
		return NimbusJwtDecoder
			.withPublicKey(rsaKey().toRSAPublicKey())
			.build();
	}

	@Bean
	public RSAKey rsaKey() {

		KeyPair keyPair = keyPair();

		return new RSAKey
			.Builder((RSAPublicKey)keyPair.getPublic())
			.privateKey((RSAPrivateKey)keyPair.getPrivate())
			.keyID(UUID.randomUUID().toString())
			.build();
	}

	@Bean
	public KeyPair keyPair() {
		try {
			var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			return keyPairGenerator.generateKeyPair();
		} catch (Exception e) {
			throw new IllegalStateException(
				"Unable to generate an RSA Key Pair", e);
		}
	}

	// 구글 로그인
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}