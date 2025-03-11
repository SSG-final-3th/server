package com.exam.user;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class User implements Persistable<String> {

	@Id
	@Column(nullable = false)
	String userid;  // 아이디 (primary key)

	@Column(nullable = false)
	String passwd;  // 비밀번호

	@Column(nullable = false)
	String username;  // 사용자 이름
	String post;  // 주소
	String addr1;  // 주소1
	String addr2;  // 주소2
	String phoneNumber;  // 전화번호
	String email;  // 이메일
	String role = "USER";  // 역할, 기본값 'USER'

	@CreatedDate
	@Transient //테이블의 컬럼으로 안만들어짐, 순수하게 시간만
	private LocalDateTime createdDate;

	@Override
	public String getId() {
		return userid;
	}

	@Override
	public boolean isNew() {
		return createdDate == null;
	}
}
