package com.exam.orderinfo;

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
	private String receiverName;
	private String post;
	private String addr1;
	private String addr2;
	private String phoneNumber;
}
