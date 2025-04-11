package com.exam.orderinfo;

import java.time.LocalDate;
import java.util.List;

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
public class OrderInfoDTO {

	private Long orderId;
	private String userId;
	private String productCode;
	private int quantity;
	private List<ProductOrderDTO> orders;
	private String receiverName;
	private String post;
	private String addr1;
	private String addr2;
	private String phoneNumber;
	private String productName;
	private String image;

	private String impUid;

	private int orderPrice;
	private int discountedPrice; // 할인된 가격

	String selectedCoupon; // 추가된 부분 (쿠폰 정보)
	@Builder.Default
	LocalDate orderDate = LocalDate.now();

	// 주문을 처리한 매장 정보
	private String branchName;
}