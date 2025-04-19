package com.exam.product;

import org.apache.ibatis.type.Alias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Alias("productDTO")
public class ProductDTO {

	@NotBlank(message = "productCode필수")
	private String productCode;

	@NotBlank(message = "category 필수")
	private String category;

	@NotBlank(message = "productName 필수")
	private String productName;

	@NotBlank(message = "description 필수")
	private String description;

	@NotNull(message = "price 필수")
	private int price;

	@NotBlank(message = "image 필수")
	private String image;

	private double averageRating;

	public ProductDTO(String productCode, String productName, String category, int price) {
		this.productCode = productCode;
		this.productName = productName;
		this.category = category;
		this.price = price;
	}
}
