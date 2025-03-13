package com.exam.mypage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exam.user.User;

public interface MypageRepository extends JpaRepository<User, String> {
	//  userId로 회원 정보 조회
	Optional<User> findByuserId(String userId);
}
