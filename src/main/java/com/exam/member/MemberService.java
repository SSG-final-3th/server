package com.exam.member;

public interface MemberService {

	// 회원가입
	public void save(MemberDTO dto);

	// mypage
	public MemberDTO findById(String userid);

	// 로그인
	public MemberDTO findByUserid(String userid);

	//아이디찾기
	public String findUseridByNameAndEmail(String username, String email);

	//비밀번호재설정
	public boolean resetPassword(String userid, String phoneNumber, String newPassword);
}
