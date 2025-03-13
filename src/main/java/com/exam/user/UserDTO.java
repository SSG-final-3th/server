package com.exam.user;

import java.time.LocalDate;

import org.apache.ibatis.type.Alias;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
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
@Alias("UserDTO")
public class UserDTO {

	@NotBlank(message = "User ID 필수")
	String userId;  // 아이디 (primary key)

	@NotBlank(message = "User passwd 필수")
	String passwd;  // 비밀번호

	@NotBlank(message = "User username 필수")
	String username;  // 사용자 이름

	String post;  // 주소
	String addr1;  // 주소1
	String addr2;  // 주소2
	String phoneNumber;  // 전화번호

	String email;  // 이메일

	String role = "USER";  // 역할, 기본값 'USER'
	String newPassword;

	@CreationTimestamp
	@Column(updatable = false) //저장할때만 자동저장O 수정할때는 저장 X 을위한것
	LocalDate createDate; //저장할때만 자동저장O 수정할때는 저장 X

	public void updatePassword(String newPassword) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		this.passwd = encoder.encode(newPassword);  // 비밀번호 암호화 후 업데이트
	}
}

