package com.exam.social.naver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
@RequiredArgsConstructor
public class NaverServiceImpl implements NaverService {

	@Value("${naver.client.id}")
	private String clientId;

	@Value("${naver.client.secret}")
	private String clientSecret;

	@Value("${naver.redirect.uri}")
	private String redirectUri;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 네이버 로그인 URL 반환
	 */
	@Override
	public String getNaverLoginUrl() {
		return "https://nid.naver.com/oauth2.0/authorize?response_type=code"
			+ "&client_id=" + clientId
			+ "&redirect_uri=" + redirectUri
			+ "&state=RANDOM_STATE";
	}

	/**
	 * 네이버에서 액세스 토큰 요청
	 */
	@Override
	public String getAccessToken(String code, String state) throws Exception {
		String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
			+ "&client_id=" + clientId
			+ "&client_secret=" + clientSecret
			+ "&code=" + code
			+ "&state=" + state;

		ResponseEntity<String> response = restTemplate.getForEntity(tokenUrl, String.class);
		JsonNode jsonNode = objectMapper.readTree(response.getBody());
		return jsonNode.get("access_token").asText();
	}

	/**
	 * 네이버에서 사용자 정보 가져오기
	 */
	@Override
	public NaverUserResponseDTO getUserInfo(String accessToken) throws Exception {
		String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);
		JsonNode jsonNode = objectMapper.readTree(response.getBody()).get("response");

		return new NaverUserResponseDTO(
			jsonNode.get("id").asText(),
			jsonNode.get("email").asText(),
			jsonNode.get("name").asText()

		);
	}
}
