package com.exam.admin;

import java.time.LocalDateTime;

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
@Table(name = "goods")
public class Goods {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "goodsId", nullable = false)
	private int goodsId;  // goodsId는 자동 증가 값

	@Column(name = "productCode", nullable = false)
	private String productCode;  // 상품 코드 (productCode)

	@Column(name = "branchName", nullable = false)
	private String branchName;  // 지점 이름 (branchName)

	@Column(name = "expirationDate", nullable = false)
	private LocalDateTime expirationDate;  // 유효기간 (expirationDate)

}
