package com.exam.coupon;

import java.time.LocalDate;
import java.util.List;

public interface CouponService {

	//회원가입 시 쿠폰 저장
	void addNewMemberOnlineCoupon(String userId);

	void addNewMemberOfflineCoupon(String userId);

	//userid로 쿠폰 찾기
	List<CouponDTO> findByUserId(String userId);

	//쿠폰 자동삭제 로직
	void deleteExpiredCoupons(LocalDate now);

	//월 5만원 이상 구매 시 월1일 쿠폰 지급
	void addMonthCoupon(String userId);

	//주문 완료 후 쿠폰 삭제
	void deleteByCouponId(Integer couponId);



	Coupon enhanceCoupons(Coupon couponId1, Coupon couponId2);

	Coupon findCouponById(Integer couponId1);
}