package com.exam.orderinfo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductOrderDTO {
	private String productCode;
	private int quantity;
}
// 장바구니에서 여러개의 상품이 리스트 형태로 오는 것을 처리하기 위한 DTO임