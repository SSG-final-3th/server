package com.exam.social.naver;

import com.exam.security.JwtTokenService;
import com.exam.user.UserDTO;
import com.exam.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("login/naver")
public class NaverController {

	private final NaverService naverService;
	private final JwtTokenService jwtTokenService;
	private final UserService userService;

	public NaverController(NaverService naverAuthService, JwtTokenService jwtTokenService, UserService userService) {
		this.naverService = naverAuthService;
		this.jwtTokenService = jwtTokenService;
		this.userService = userService;
	}

	/**
	 * 네이버 로그인 URL을 JSON 형식으로 반환
	 */
	@GetMapping("/login")
	public ResponseEntity<Map<String, String>> getNaverLoginUrl() {
		Map<String, String> response = new HashMap<>();
		response.put("url", naverService.getNaverLoginUrl());
		return ResponseEntity.ok(response);
	}

	/**
	 * 네이버 로그인 콜백 처리 & JWT 발급 후 프론트엔드로 리다이렉트
	 */
	@GetMapping("")
	public void handleNaverCallback(@RequestParam("code") String code,
		@RequestParam("state") String state,
		HttpServletResponse response) throws IOException {
		try {
			// 1️⃣ 네이버에서 액세스 토큰 받기
			String accessToken = naverService.getAccessToken(code, state);

			// 2️⃣ 네이버 사용자 정보 가져오기
			NaverUserResponseDTO userInfo = naverService.getUserInfo(accessToken);

			// 3️⃣ DB에서 사용자 조회 or 생성
			UserDTO user = userService.findOrCreateUser(userInfo);

			// 4️⃣ JWT 발급
			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(user.getUserId(), null,
					Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())));

			String token = jwtTokenService.generateToken(authentication);

			// 5️⃣ 프론트엔드로 리다이렉트
			// NaverController.java
			response.sendRedirect("https://moreandmall.click/login/naver/callback?token=" + token);


		} catch (Exception e) {
			response.sendRedirect("https://moreandmall.click//login?error=" + e.getMessage());
		}
	}
}
