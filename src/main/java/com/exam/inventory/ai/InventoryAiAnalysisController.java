package com.exam.inventory.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/inventory-analysis")
@RequiredArgsConstructor
@Slf4j
public class InventoryAiAnalysisController {

	private final InventoryAiAnalysisService analysisService;

	//지점별 해당제품 AI 분석
	@GetMapping("/{productCode}/{branchName}")
	public ResponseEntity<InventoryAnalysisResponseDTO> analyzeInventory(
		@PathVariable String productCode,
		@PathVariable String branchName) {

		InventoryAlertDTO alert = analysisService.analyzeBranchInventoryTrend(productCode, branchName);

		InventoryAnalysisResponseDTO response = new InventoryAnalysisResponseDTO(
			productCode,
			branchName,
			alert
		);

		return ResponseEntity.ok(response);
	}

	// 전국 해당 제품 AI 분석
	@GetMapping("/nationwide/{productCode}")
	public ResponseEntity<InventoryAnalysisResponseDTO> analyzeNationwideInventory(
		@PathVariable String productCode) {

		InventoryAlertDTO alert = analysisService.analyzeNationwideInventoryTrend(productCode);

		InventoryAnalysisResponseDTO response = new InventoryAnalysisResponseDTO(
			productCode,
			"전국", // branchName 대신 '전국'
			alert
		);

		return ResponseEntity.ok(response);
	}
}