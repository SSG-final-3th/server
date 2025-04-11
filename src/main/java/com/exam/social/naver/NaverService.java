package com.exam.social.naver;

public interface NaverService {
	String getNaverLoginUrl();
	String getAccessToken(String code, String state) throws Exception;
	NaverUserResponseDTO getUserInfo(String accessToken) throws Exception;
}
