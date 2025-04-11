package com.exam.user;

import com.exam.social.naver.NaverUserResponseDTO;

public interface UserService {

	// 회원가입
	public void save(UserDTO dto);

	// mypage
	public UserDTO findById(String userId);

	// 로그인 아이디 중복 확인
	public UserDTO findByuserId(String userId);

	public UserDTO findByUserNameAndEmail(String userName, String email);

	//비밀번호재설정
	public boolean resetPassword(String userId, String phoneNumber, String newPassword);

	UserDTO getUserProfile(String userId);

	UserDTO findOrCreateUser(NaverUserResponseDTO userInfo);

	User saveOrLoginNaverUser(NaverUserResponseDTO dto);

}