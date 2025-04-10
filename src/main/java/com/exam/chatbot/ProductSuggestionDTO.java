package com.exam.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSuggestionDTO {
	private String productCode;
	private String productName;
	private String category;
	private String description;
	private int price;
	private String image;
	private double averageRating;
	private String reason;
}