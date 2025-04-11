package com.exam.coupon;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupon")
public class CouponController {

	CouponService couponService;

	public CouponController(CouponService couponService) {
		this.couponService = couponService;
	}

	//새로 가입시 온라인 쿠폰 발급
	@GetMapping("/newMemberOnlineCoupon/{userId}")
	public void newMemberOnlineCoupon(@PathVariable String userId) {
		couponService.addNewMemberOnlineCoupon(userId);
	}

	//새로 가입시 오프라인 쿠폰 발급
	@GetMapping("/newMemberOfflineCoupon/{userId}")
	public void newMemberOfflineCoupon(@PathVariable String userId) {
		couponService.addNewMemberOfflineCoupon(userId);
	}

	@PostMapping("/merge")
	public ResponseEntity<?> mergeCoupons(@RequestBody MergeCouponRequest request) {
		try {
			Coupon coupon1 = couponService.findCouponById(request.getCouponId1());
			Coupon coupon2 = couponService.findCouponById(request.getCouponId2());

			Coupon result = couponService.enhanceCoupons(coupon1, coupon2);

			if (result != null) {
				return ResponseEntity.ok(result);  // 강화된 쿠폰 반환
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("쿠폰 강화 실패!");
			}
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("에러: " + e.getMessage());
		}
	}


	//userId로 쿠폰 list 가져오기
	@GetMapping("/list")
	public ResponseEntity<?> findAllCouponsByUserId() {
		String userId = getAuthenticatedUserId(); // JWT에서 userId 가져오기
		if (userId == null) {
			return ResponseEntity.status(401).body("인증되지 않은 사용자");
		}
		List<CouponDTO> couponDTOList = couponService.findByUserId(userId);
		return ResponseEntity.status(200).body(couponDTOList);
	}

	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	//월초 쿠폰 발급 / 얘는 지금은 없어도 되는건가..?
	//@GetMapping("/monthReward/{userId}")
	// public void newMonthCoupon(@PathVariable String userId) {
	// 	couponService.addMonthCoupon(userId);
	// }

	@DeleteMapping("/delete/{couponId}")
	public ResponseEntity<String> deleteCoupon(@PathVariable Integer couponId) {
		try {
			couponService.deleteByCouponId(couponId);
			return ResponseEntity.ok("사용한 쿠폰이 삭제되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("쿠폰 삭제에 실패했습니다.");
		}

	}

}
