package com.exam.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotRequestDTO {
	private String message;
	private String sessionId;
	private String productCode; // 선택적 - 특정 제품에 대한 질문인 경우
}