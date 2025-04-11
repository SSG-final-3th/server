package com.exam.chatbot;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotResponseDTO {
	private String message;
	private String sessionId;
	private boolean isProductRecommendation;
	private List<ProductSuggestionDTO> suggestedProducts;
}
