package com.exam.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	UserService userService;
	//repository 생성자주입
	UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public void save(UserDTO dto) {
		// MemberDTO --> Member 로 변환하는 작업 필요
		User user = User.builder()
			.userId(dto.getUserId())
			.passwd(dto.getPasswd())
			.username(dto.getUsername())
			.post(dto.getPost())
			.addr1(dto.getAddr1())
			.addr2(dto.getAddr2())
			.phoneNumber(dto.getPhoneNumber())
			.email(dto.getEmail())
			.role(dto.getRole())
			.build();

		userRepository.save(user);
	}

	@Override
	public UserDTO findById(String userId) {
		User user = userRepository.findById(userId).orElse(null);
		if (user == null)
			return null;

		return convertToDTO(user);
	}

	//  로그인 기능
	@Override
	public UserDTO findByuserId(String userId) {
		User user = userRepository.findByuserIdAndPasswd(userId, ""); // 더미 비밀번호
		if (user == null)
			return null;

		return convertToDTO(user);
	}

	//  Entity → DTO 변환 메서드
	private UserDTO convertToDTO(User user) {
		return UserDTO.builder()
			.userId(user.getUserId())
			.passwd(user.getPasswd()) // 비밀번호도 함께 반환
			.username(user.getUsername())
			.post(user.getPost())
			.addr1(user.getAddr1())
			.addr2(user.getAddr2())
			.phoneNumber(user.getPhoneNumber())
			.email(user.getEmail())
			.role(user.getRole())
			.build();
	}

	@Override
	public String finduserIdByNameAndEmail(String username, String email) {
		User user = userRepository.findByUsernameAndEmail(username, email); // repository에서 호출해야 함
		if (user == null) {
			throw new IllegalArgumentException("일치하는 회원 정보가 없습니다.");
		}
		return user.getUserId(); // 아이디 반환
	}

	@Override
	@Transactional
	public boolean resetPassword(String userId, String phoneNumber, String newPassword) {
		User user = userRepository.findByuserIdAndPhoneNumber(userId, phoneNumber);
		if (user != null) {
			String encodedPassword = new BCryptPasswordEncoder().encode(newPassword);
			user.setPasswd(encodedPassword); // 비밀번호 변경
			//userRepository.save(user);
			return true; // 변경 성공
		}

		return false; // 일치하는 정보가 없으면 false 반환
	}

}
