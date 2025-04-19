package com.exam.user;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//lombok 으로 AllArgsConstructor~Builder 까지 총 6개 주기
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "user")
public class User {

	@Id
	@Column(nullable = false)
	String userId;  // 아이디 (primary key)

	@Column(nullable = false)
	String password;  // 비밀번호

	@Column(nullable = false, unique = true)
	String userName;  // 사용자 이름
	String post;  // 주소
	String addr1;  // 주소1
	String addr2;  // 주소2
	String phoneNumber;  // 전화번호
	String email;  // 이메일

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	Role role = Role.USER;   // 역할, 기본값 'USER'

	@Transient
	String newPassword;
	@CreationTimestamp
	@Column(updatable = false)   //저장할때만 자동저장O 수정할때는 저장 X 을위한것
	LocalDate createDate;   //저장할때만 자동저장O 수정할때는 저장 X

	@Column(unique = true)
	private Long kakaoId; // 카카오 로그인 전용 ID

	@Column(unique = true)
	private String googleId;
	@Column(unique = true)
	private String naverId;
}
