package com.exam.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "products")
public class Product {

	@Id
	private String productCode;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private String productName;

	private String description;

	@Column(nullable = false)
	private int price;
	private String image;



}
