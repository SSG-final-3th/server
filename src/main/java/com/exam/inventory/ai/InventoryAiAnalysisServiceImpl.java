package com.exam.inventory.ai;

import com.exam.inventory.InventoryLog;
import com.exam.inventory.InventoryLogRepository;
import com.exam.product.Product;
import com.exam.product.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAiAnalysisServiceImpl implements InventoryAiAnalysisService {

	private final InventoryLogRepository inventoryLogRepository;
	private final ProductRepository productRepository;
	private final ChatClient chatClient; //properties에서 API키 등록해놓으면 Spring AI가 알아서 빈으로 등록해줌



	//전국적으로다가 분석
	@Override
	public InventoryAlertDTO analyzeNationwideInventoryTrend(String productCode) {
		List<InventoryLog> logs = inventoryLogRepository.findByProductCodeOrderByChangeDate(productCode);
		return analyzeInternal(productCode, logs);
	}

	//지점적으로다가 분석
	@Override
	public InventoryAlertDTO analyzeBranchInventoryTrend(String productCode, String branchName) {
		List<InventoryLog> logs = inventoryLogRepository.findByProductCodeAndBranchNameOrderByChangeDateAsc(productCode, branchName);
		return analyzeInternal(productCode, logs);
	}


	private InventoryAlertDTO analyzeInternal(String productCode, List<InventoryLog> logs) {
		Product product = productRepository.findByProductCode(productCode);
		if (product == null) {
			throw new RuntimeException("상품을 찾을 수 없습니다: " + productCode);
		}
		if (logs == null || logs.isEmpty()) {
			log.warn("No inventory logs found for productCode: {}", productCode);
			return new InventoryAlertDTO(false, "재고 로그 없음", "분석할 데이터가 부족합니다.", 0);
		}
		// 프롬프트를 담을 문자열 프롬프트 객체 생성
		String prompt = buildUserPrompt(product.getProductName(), logs);
		String response = requestAi(prompt);
		return parseAiResponse(response);
	}
	//AI에게 보낼 자연어 문자열 프롬프트 생성하는 메소드
	private String buildUserPrompt(String productName, List<InventoryLog> logs) {
		StringBuilder sb = new StringBuilder();
		sb.append("상품명: ").append(productName).append("\n재고 로그:\n");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		Map<String, Integer> latestPerDay = new TreeMap<>();
		logs.forEach(log -> {
			String date = log.getChangeDate().toLocalDate().format(formatter);
			latestPerDay.put(date, log.getRemainingStock());
		});
		latestPerDay.forEach((date, stock) -> sb.append("- ").append(date).append(": ").append(stock).append("개\n"));

		sb.append("\n위 데이터를 분석해주세요.");
		return sb.toString();
	}
	/*
		상품명: 제주 한돈
			재고 로그:
			- 2025-04-01: 50개
			- 2025-04-02: 30개
			- 2025-04-03: 15개
			- 2025-04-04: 5개

	 */


	private String requestAi(String userPrompt) {
		SystemMessage systemMessage = new SystemMessage(
			"당신은 재고 관리 전문가입니다. 아래 재고 로그 데이터를 분석하여 다음 정보를 JSON으로 응답하세요:\n" +
				"1. anomaly: 재고 급감 또는 이상 징후 여부 (true/false)\n" +
				"2. trendSummary: 재고 흐름 요약\n" +
				"3. recommendation: 관리자 조치 제안\n" +
				"4. riskScore: 위험 점수 (0~100)\n" +
				"꼭 반드시 불필요한 텍스트 없이 JSON으로만 응답하세요."
		);

		UserMessage userMessage = new UserMessage(userPrompt);
		Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

		try {
			return chatClient.call(prompt).getResult().getOutput().getContent();
		} catch (Exception e) {
			log.error("AI 호출 중 오류: {}", e.getMessage(), e);
			throw new RuntimeException("AI 분석 실패");
		}
	}

	private InventoryAlertDTO parseAiResponse(String jsonResponse) {

		try {
			// 어쩌다 한번씩 유효하지 않은 json 응답이 올때가 있음 그거 대비 로그임 예를 들어 { 로 시작하지 않는다던가
			if (jsonResponse == null || !jsonResponse.trim().startsWith("{")) {
				log.error("AI 응답이 유효한 JSON이 아닙니다: {}", jsonResponse);
				throw new RuntimeException("AI 응답 포맷 오류");
			}
			log.error("📥 AI 원본 응답:\n{}", jsonResponse);

			JSONObject json = new JSONObject(jsonResponse);
			return new InventoryAlertDTO(
				json.optBoolean("anomaly", false),
				json.optString("trendSummary", "정보 없음"),
				json.optString("recommendation", "추천 없음"),
				json.optInt("riskScore", 0)
			);
		} catch (Exception e) {
			log.error("AI 응답 파싱 오류: {}", e.getMessage(), e);
			throw new RuntimeException("AI 응답 파싱 실패");
		}
	}
}