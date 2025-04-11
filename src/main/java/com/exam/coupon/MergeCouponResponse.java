package com.exam.coupon;

import lombok.Data;

@Data
public class MergeCouponResponse {
	private boolean success;
	private String message;
	private Coupon mergedCoupon;
}
