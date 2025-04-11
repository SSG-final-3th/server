package com.exam.coupon;

import lombok.Data;

@Data
public class MergeCouponRequest {
	private String userId;
	private Integer  couponId1;
	private Integer  couponId2;
	private String couponType; // "online" 또는 "offline"
}

