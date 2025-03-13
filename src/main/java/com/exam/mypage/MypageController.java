package com.exam.mypage;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mypage")
public class MypageController {

	private final MypageService mypageService;

	public MypageController(MypageService mypageService) {
		this.mypageService = mypageService;
	}

	// ResponseEntity < > 안의 물음표는 반환타입을 명시하지 않는 것
	//  마이페이지 조회
	@GetMapping("/home")
	public ResponseEntity<?> getMypage() {
		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();

		String userId = authentication.getName();
		if (userId == null) {
			return ResponseEntity.status(401).body("인증되지 않은 사용자");
		}

		MypageDTO mypage = mypageService.getMypage(userId);
		return ResponseEntity.ok(mypage);
	}

	//  회원정보 수정
	@PostMapping("/memedit")
	public ResponseEntity<?> updateMypage(@RequestBody MypageDTO dto) {
		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();

		String userId = authentication.getName();
		if (userId == null) {
			return ResponseEntity.status(401).body("인증되지 않은 사용자");
		}

		mypageService.updateMypage(userId, dto);
		return ResponseEntity.ok("회원 정보가 수정되었습니다.");
	}

	//  회원 탈퇴
	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteMypage() {

		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();

		String userId = authentication.getName();
		if (userId == null) {
			return ResponseEntity.status(401).body("인증되지 않은 사용자");
		}

		mypageService.deleteMypage(userId);
		return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
	}

	//  현재 로그인한 사용자의 ID 가져오기
}
