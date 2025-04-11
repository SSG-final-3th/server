package com.exam.orderinfo;

import java.time.LocalDate;

import jakarta.persistence.Column;
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
@Table(name = "orderinfo")  // 💡 DB 테이블명과 동일하게 설정
public class OrderInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;  // 주문 ID (Primary Key)

	private String userId;  // 사용자 ID
	private String productCode;  // 주문한 상품 코드
	private int quantity;  // 주문 수량
	private String receiverName;  // 수령인 이름
	private String post;  // 우편번호
	private String addr1;  // 주소1
	private String addr2;  // 주소2
	private String phoneNumber;  // 연락처

	@Column
	private String impUid;

	@Column(nullable = false)
	private int orderPrice;
	@Builder.Default
	LocalDate orderDate = LocalDate.now();

	// 주문을 처리한 매장 정보
	private String branchName;
}
