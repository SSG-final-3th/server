package com.exam.mypage;

import org.springframework.stereotype.Service;

import com.exam.user.User;

import jakarta.transaction.Transactional;

@Service
public class MypageServiceImpl implements MypageService {

	private final MypageRepository mypageRepository;

	public MypageServiceImpl(MypageRepository mypageRepository) {
		this.mypageRepository = mypageRepository;
	}

	@Override
	public MypageDTO getMypage(String userid) {

		User user = mypageRepository.findByUserid(userid).orElse(null);
		if (user == null) {
			return null;
		}
		return convertToDTO(user);
	}

	@Override
	@Transactional
	public void updateMypage(String userid, MypageDTO dto) {
		User user = mypageRepository.findByUserid(userid).orElse(null);
		if (user == null) {
			throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
		}

		user.setUsername(dto.getUsername());
		user.setPhoneNumber(dto.getPhoneNumber());
		user.setEmail(dto.getEmail());
		user.setPost(dto.getPost());
		user.setAddr1(dto.getAddr1());
		user.setAddr2(dto.getAddr2());

		//mypageRepository.save(user);
	}

	@Override
	@Transactional
	public void deleteMypage(String userid) {
		User user = mypageRepository.findByUserid(userid).orElse(null);
		if (user == null) {
			throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
		}

		mypageRepository.deleteById(userid);
	}

	private MypageDTO convertToDTO(User user) {
		return MypageDTO.builder()
			.userid(user.getUserid())
			.username(user.getUsername())
			.post(user.getPost())
			.addr1(user.getAddr1())
			.addr2(user.getAddr2())
			.phoneNumber(user.getPhoneNumber())
			.email(user.getEmail())
			.role(user.getRole())
			.build();
	}
}
