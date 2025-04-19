package com.exam.coupon;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name="coupon")
public class Coupon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int couponId;  // 쿠폰 고유 번호
	String userId;
	String couponName;  // 쿠폰명
	String minPrice;  // 최소 구매 금액
	LocalDate expirationDate;  // 쿠폰 유효 기간
	String benefits;  // 쿠폰 혜택
	String couponType;

}

