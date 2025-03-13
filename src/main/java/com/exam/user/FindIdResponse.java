package com.exam.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserController {
	UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/home")
	public ResponseEntity<String> home() {
		return ResponseEntity.status(200).body("home"); // 200 OK 상태 코드와 함께 "home" 반환
	}

    /*
	 	headers : Content-Type:application/json
	 	body :  {
				   "userId":"llsj09",
				   "passwd":"1234",
				   "username":"이소정",
				   "post": "당감주공",
				   "addr1": "205동",
				   "addr2": "605호",
				   "phoneNumber": "01077389232",
				   "email": "llsj08@naver.com"

				}
	 */

	@PostMapping("/signup")
	public ResponseEntity<UserDTO> signup(@Valid @RequestBody UserDTO dto) {
		log.info("비번 암호화전: {}", dto.getPasswd());
		//비번을 암호화 해야됨.
		String encodedPW = new BCryptPasswordEncoder().encode(dto.getPasswd());
		log.info("비번 암호화후: {}", encodedPW);

		dto.setPasswd(encodedPW);
		userService.save(dto);

		return ResponseEntity.created(null).body(dto);  // 201 상태코드 반환됨.
	}

	@PostMapping("/findid")
	public ResponseEntity<String> findId(@RequestBody UserDTO dto) {
		String userId = userService.finduserIdByNameAndEmail(dto.getUsername(), dto.getEmail());
		if (userId != null) {
			return ResponseEntity.ok(userId);
		} else {
			return ResponseEntity.status(404).body("아이디를 찾을 수 없습니다.");
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
		boolean result = userService.resetPassword(request.getUserId(), request.getPhoneNumber(),
			request.getNewPassword());

		if (result) {
			return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다.");
		} else {
			return ResponseEntity.status(404).body("아이디와 전화번호가 일치하는 회원이 없습니다.");
		}
	}
}



