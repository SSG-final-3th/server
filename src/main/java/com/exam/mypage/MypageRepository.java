package com.exam.mypage;

import com.exam.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MypageRepository extends JpaRepository<Member, String> {
	//  userid로 회원 정보 조회
	Optional<Member> findByUserid(String userid);
}
