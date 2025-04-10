package com.exam.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class JwtTokenResponse {

	//	 - 용도: token 과 로그인한 userId 저장
	//     필요시 추가 정보 저장 가능함.

	String token;
	String userId;

	String role;

}
