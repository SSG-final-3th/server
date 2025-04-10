
package com.exam.chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

	private final ChatbotService chatbotService;

	@PostMapping("/message")
	public ResponseEntity<ChatbotResponseDTO> processMessage(@RequestBody ChatbotRequestDTO request) {
		log.info("Received chatbot message: {}", request.getMessage());

		try {
			ChatbotResponseDTO response = chatbotService.processMessage(request);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error processing chatbot message: {}", e.getMessage(), e);
			return ResponseEntity.badRequest().build();
		}
	}

	@DeleteMapping("/session/{sessionId}")
	public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
		log.info("Clearing chatbot session: {}", sessionId);

		try {
			chatbotService.clearSession(sessionId);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("Error clearing chatbot session: {}", e.getMessage(), e);
			return ResponseEntity.badRequest().build();
		}
	}
}