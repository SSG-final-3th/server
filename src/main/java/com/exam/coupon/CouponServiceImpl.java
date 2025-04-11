package com.exam.coupon;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponServiceImpl implements CouponService {

	private final CouponRepository couponRepository;

	public CouponServiceImpl(CouponRepository couponRepository) {
		this.couponRepository = couponRepository;
	}

	private Coupon createCoupon(String userId, String couponName, String minPrice, String benefits, String couponType) {
		LocalDate expirationDate = LocalDate.now().plusMonths(1);
		return Coupon.builder()
			.userId(userId)
			.couponName(couponName)
			.minPrice(minPrice)
			.expirationDate(expirationDate)
			.benefits(benefits)
			.couponType(couponType)
			.build();
	}

	@Override
	public void addNewMemberOnlineCoupon(String userId) {
		Coupon coupon = createCoupon(userId, "[온라인] 5% 할인 쿠폰", "30000", "5% 할인", "online");
		couponRepository.save(coupon);
	}
	@Override
	public Coupon findCouponById(Integer couponId) {
		return couponRepository.findById(couponId)
			.orElseThrow(() -> new IllegalArgumentException("해당 ID의 쿠폰이 없습니다: " + couponId));
	}

	@Override
	public void addNewMemberOfflineCoupon(String userId) {
		Coupon coupon = createCoupon(userId, "[오프라인] 10% 할인 쿠폰", "30000", "10% 할인", "offline");
		couponRepository.save(coupon);
	}

	@Override
	public void addMonthCoupon(String userId) {
		Coupon coupon = createCoupon(userId, "[오프라인] 20% 할인 쿠폰", "30000", "20% 할인", "offline");
		couponRepository.save(coupon);
	}
	@Override
	public Coupon enhanceCoupons(Coupon coupon1, Coupon coupon2) {
		int discount1 = extractNumberFromBenefits(coupon1.getBenefits());
		int discount2 = extractNumberFromBenefits(coupon2.getBenefits());

		int successRate = 100 - (discount1 + discount2);
		int random = new Random().nextInt(100) + 1;

		couponRepository.delete(coupon1);
		couponRepository.delete(coupon2);

		if (random <= successRate) {
			int enhancedDiscount = discount1 + discount2;
			String enhancedName = enhancedDiscount + "% 할인 쿠폰";

			Coupon enhancedCoupon = Coupon.builder()
				.userId(coupon1.getUserId())
				.couponName(enhancedName)
				.minPrice("10000")
				.expirationDate(LocalDate.now().plusDays(30))
				.benefits(enhancedDiscount + "%")
				.couponType(coupon1.getCouponType())
				.build();

			return couponRepository.save(enhancedCoupon);
		} else {
			return null;
		}
	}

	// 숫자만 추출하는 메서드
	private int extractNumberFromBenefits(String benefits) {
		return Integer.parseInt(benefits.replaceAll("[^0-9]", ""));
	}

	@Override
	public List<CouponDTO> findByUserId(String userId) {
		return couponRepository.findByUserId(userId).stream()
			.map(c -> CouponDTO.builder()
				.couponId(c.getCouponId())
				.userId(c.getUserId())
				.couponName(c.getCouponName())
				.minPrice(c.getMinPrice())
				.expirationDate(c.getExpirationDate())
				.benefits(c.getBenefits())
				.couponType(c.getCouponType())
				.build())
			.collect(Collectors.toList());
	}

	@Override
	public void deleteExpiredCoupons(LocalDate now) {
		couponRepository.deleteByExpirationDateBefore(now);
	}

	@Override
	@Transactional
	public void deleteByCouponId(Integer couponId) {
		couponRepository.deleteByCouponId(couponId);
	}



	// 할인율 문자열에서 숫자만 추출하는 유틸 메서드 추가
	private int extractDiscountPercentage(String benefits) {
		try {
			return Integer.parseInt(benefits.replaceAll("[^0-9]", ""));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("할인율을 추출할 수 없습니다: " + benefits);
		}
	}
}
