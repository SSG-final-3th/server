package com.exam.mypage;

public interface MypageService {

	//  마이페이지 조회
	MypageDTO getMypage(String userId);

	//  회원정보 수정
	void updateMypage(String userId, MypageDTO dto);

	// 회원 탈퇴
	void deleteMypage(String userId);
}