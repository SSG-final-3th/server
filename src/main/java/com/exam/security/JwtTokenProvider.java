package com.exam.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.exam.user.UserDTO;
import com.exam.user.UserService;

//용도: 입력된 id와 pw 이용해서 DB와 연동해서 검증작업후 최종적으로  token 반환해주는 역할.

@Component
public class JwtTokenProvider {

	UserService userService;
	JwtTokenService tokenService;

	public JwtTokenProvider(UserService userService, JwtTokenService tokenService) {
		this.userService = userService;
		this.tokenService = tokenService;
	}

	public String authenticate(Map<String, String> map) {
		String encodedtoken = null;

		String userId = map.get("userId");
		String passwd = map.get("passwd");

		UserDTO dto = userService.findById(userId);

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		UsernamePasswordAuthenticationToken token = null;
		if (dto != null && passwordEncoder.matches(passwd, dto.getPasswd())) {

			UserDTO new_dto = new UserDTO();
			new_dto.setUserId(userId);
			new_dto.setPasswd(passwd); // 1234
			new_dto.setUsername(dto.getUsername());

			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("USER"));

			// 다음 token 정보가 세션에 저장된다.
			// dto 값을 사용하면 나중에 문자열로 "MemberDTO { userId:kim4832 ~"
			//			token = new UsernamePasswordAuthenticationToken(new_dto, null, authorities);

			// 나중에 userId 값이 필요한데 쉽게 얻기 위해서 userId를 지정함.
			token = new UsernamePasswordAuthenticationToken(userId, null, authorities);

			// Authentication 을 이용해서 token 생성
			encodedtoken = tokenService.generateToken(token);

		}//end if

		return encodedtoken;
	}
}
