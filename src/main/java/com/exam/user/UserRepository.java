package com.exam.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    	/*
		 JpaRepository에서 기본으로 제공하는 CRUD 메서드 사용 가능

		- 전체 엔티티 조회 : findAll() - 리턴타입: List
		- 특정 엔티티 조회 : findById(ID id) - 리턴타입: Optional
		- 엔티티 저장 : save(entity)
		- 전체 엔티티 삭제 : deleteAll()
		- 특정 엔티티 id로 삭제 : deleteById(ID id)
		- 특정 엔티티 엔티티로 삭제 : delete(T entity)
		- 엔티티 수정 : 메서드 지원 없이 더티체킹 이용
		- 엔티티 갯수 : count()
	    */

	// 로그인 검증을 위한 쿼리 메서드
	User findByuserIdAndPassword(String userId, String password);

	User findByuserIdAndPhoneNumber(String userId, String phoneNumber);

	User findByUserNameAndEmail(String userName, String email);

	Optional<User> findByKakaoId(Long kakaoId);

	Optional<User> findByEmail(String email);

	Optional<User> findByNaverId(String naverId);


}