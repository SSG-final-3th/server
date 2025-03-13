package com.exam.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PasswordResetRequest {

	private String userId;        // 사용자 아이디
	private String phoneNumber;   // 사용자 전화번호
	private String newPassword;   // 새 비밀번호
}