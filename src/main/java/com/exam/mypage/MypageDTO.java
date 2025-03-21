package com.exam.mypage;

import com.exam.user.Role;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class MypageDTO {

	private String userId;
	private String username;  // 사용자 이름
	private String passwd;
	private String post;  // 주소
	private String addr1;  // 주소1
	private String addr2;  // 주소2
	private String phoneNumber;  // 전화번호
	private String email;  // 이메일
	@Enumerated(EnumType.STRING)  // Enum 값을 String으로 저장
	@Column(nullable = false)
	Role role;
}