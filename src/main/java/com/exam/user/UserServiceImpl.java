package com.exam.user;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.exam.social.naver.NaverUserResponseDTO;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public void save(UserDTO dto) {
		if (userRepository.findById(dto.getUserId()).isPresent()) {
			throw new RuntimeException("이미 존재하는 userId입니다: " + dto.getUserId());
		}

		User user = User.builder()
			.userId(dto.getUserId())
			.password(dto.getPassword()) // 비밀번호 암호화
			.userName(dto.getUserName())
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
		return userRepository.findById(userId)
			.map(this::convertToDTO)
			.orElse(null);
	}

	@Override
	public UserDTO findByuserId(String userId) {
		User user = userRepository.findByuserIdAndPassword(userId, "");
		return user != null ? convertToDTO(user) : null;
	}

	@Override
	public UserDTO findByUserNameAndEmail(String userName, String email) {
		User user = userRepository.findByUserNameAndEmail(userName, email);
		if (user == null) {
			throw new IllegalArgumentException("일치하는 회원 정보가 없습니다.");
		}
		return UserDTO.builder()
			.userId(user.getUserId())
			.createDate(user.getCreateDate())
			.build();
	}

	@Override
	@Transactional
	public boolean resetPassword(String userId, String phoneNumber, String newPassword) {
		User user = userRepository.findByuserIdAndPhoneNumber(userId, phoneNumber);
		if (user != null) {
			user.setPassword(passwordEncoder.encode(newPassword));
			return true;
		}
		return false;
	}

	@Override
	public UserDTO getUserProfile(String userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

		return convertToDTO(user);
	}

	@Override
	@Transactional
	public User saveOrLoginNaverUser(NaverUserResponseDTO dto) {
		return userRepository.findByNaverId(dto.getId())
			.orElseGet(() -> userRepository.save(
				User.builder()
					.userId(dto.getId())
					.naverId(dto.getId())
					.email(dto.getEmail())
					.userName(dto.getName())
					.password("") // 소셜 로그인: 비밀번호 없음
					.role(Role.USER)
					.build()
			));
	}

	@Override
	@Transactional
	public UserDTO findOrCreateUser(NaverUserResponseDTO dto) {
		User user = userRepository.findByEmail(dto.getEmail())
			.orElseGet(() -> userRepository.save(
				User.builder()
					.userId(dto.getId())
					.userName(dto.getName())
					.email(dto.getEmail())
					.password("") // 소셜 로그인용
					.role(Role.USER)
					.build()
			));
		return convertToDTO(user);
	}

	// 공통 변환 로직
	private UserDTO convertToDTO(User user) {
		return UserDTO.builder()
			.userId(user.getUserId())
			.password(user.getPassword())
			.userName(user.getUserName())
			.post(user.getPost())
			.addr1(user.getAddr1())
			.addr2(user.getAddr2())
			.phoneNumber(user.getPhoneNumber())
			.email(user.getEmail())
			.role(user.getRole())
			.createDate(user.getCreateDate())
			.build();
	}
}
