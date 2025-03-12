package com.exam.member;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class MemberServiceImpl implements MemberService {

	// repository 생성자 주입
	MemberRepository memberRepository;

	public MemberServiceImpl(MemberRepository memberRepository) {
		super();
		this.memberRepository = memberRepository;
	}

	@Override
	@Transactional
	public void save(MemberDTO dto) {
		// MemberDTO → Member 변환
		Member member = Member.builder()
			.userid(dto.getUserid())
			.passwd(dto.getPasswd())
			.username(dto.getUsername())
			.post(dto.getPost())
			.addr1(dto.getAddr1())
			.addr2(dto.getAddr2())
			.phoneNumber(dto.getPhoneNumber())
			.email(dto.getEmail())
			.role(dto.getRole())
			.build();

		memberRepository.save(member);
	}

	@Override
	public boolean resetPassword(String userid, String phoneNumber, String newPassword) {
		Member member = memberRepository.findByUseridAndPhoneNumber(userid, phoneNumber);
		if (member == null) {
			return false; // 일치하는 정보가 없으면 false 반환
		}

		// 비밀번호 암호화
		String encodedPassword = new BCryptPasswordEncoder().encode(newPassword);
		member.setPasswd(encodedPassword); // 비밀번호 변경
		memberRepository.save(member);

		return true; // 변경 성공
	}

	@Override
	public MemberDTO findById(String userid) {
		Member member = memberRepository.findById(userid).orElse(null); // repository에서 호출해야 함
		if (member == null)
			return null;

		return convertToDTO(member);
	}

	// 로그인 기능
	@Override
	public MemberDTO findByUserid(String userid) {
		Member member = memberRepository.findByUseridAndPasswd(userid, ""); // 더미 비밀번호
		if (member == null)
			return null;

		return convertToDTO(member);
	}

	@Override
	public String findUseridByNameAndEmail(String username, String email) {
		Member member = memberRepository.findByUsernameAndEmail(username, email); // repository에서 호출해야 함
		if (member == null) {
			throw new IllegalArgumentException("일치하는 회원 정보가 없습니다.");
		}
		return convertToDTO(member);
	}

	// ✅ convertToDTO()를 클래스 내부에 배치
	private MemberDTO convertToDTO(Member member) {
		return MemberDTO.builder()
			.userid(member.getUserid())
			.passwd(member.getPasswd()) // 비밀번호도 함께 반환
			.username(member.getUsername())
			.post(member.getPost())
			.addr1(member.getAddr1())
			.addr2(member.getAddr2())
			.phoneNumber(member.getPhoneNumber())
			.email(member.getEmail())
			.role(member.getRole())
			.createDate(member.getCreateDate())
			.build();
	}
}
