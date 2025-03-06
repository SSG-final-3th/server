package com.exam.mypage;

import com.exam.member.Member;
import org.springframework.stereotype.Service;

@Service
public class MypageServiceImpl implements MypageService {

	private final MypageRepository mypageRepository;

	public MypageServiceImpl(MypageRepository mypageRepository) {
		this.mypageRepository = mypageRepository;
	}

	@Override
	public MypageDTO getMypage(String userid) {

		Member member = mypageRepository.findByUserid(userid).orElse(null);
		if (member == null) {
			return null;
		}
		return convertToDTO(member);
	}

	@Override
	public void updateMypage(String userid, MypageDTO dto) {
		Member member = mypageRepository.findByUserid(userid).orElse(null);
		if (member == null) {
			throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
		}

		member.setUsername(dto.getUsername());
		member.setPhoneNumber(dto.getPhoneNumber());
		member.setEmail(dto.getEmail());
		member.setPost(dto.getPost());
		member.setAddr1(dto.getAddr1());
		member.setAddr2(dto.getAddr2());

		mypageRepository.save(member);
	}

	@Override
	public void deleteMypage(String userid) {
		Member member = mypageRepository.findByUserid(userid).orElse(null);
		if (member == null) {
			throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
		}

		mypageRepository.deleteById(userid);
	}

	private MypageDTO convertToDTO(Member member) {
		return MypageDTO.builder()
			.userid(member.getUserid())
			.username(member.getUsername())
			.post(member.getPost())
			.addr1(member.getAddr1())
			.addr2(member.getAddr2())
			.phoneNumber(member.getPhoneNumber())
			.email(member.getEmail())
			.role(member.getRole())
			.build();
	}
}
