package com.exam.social.naver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverUserResponseDTO {
	private String id;         // 네이버 회원 ID
	private String email;      // 이메일
	private String name;       // 이름


	// 네이버에서 받아온 데이터 매핑
	public NaverUserResponseDTO(String id, String email, String name) {
		this.id = id;
		this.email = email;
		this.name = name;

	}
}
