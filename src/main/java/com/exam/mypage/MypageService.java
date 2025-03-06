package com.exam.mypage;

public interface MypageService {

	//  마이페이지 조회
	MypageDTO getMypage(String userid);

	//  회원정보 수정
	void updateMypage(String userid, MypageDTO dto);

	// 회원 탈퇴
	void deleteMypage(String userid);
}
