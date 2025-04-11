package com.exam.inventory.ai;

public interface InventoryAiAnalysisService {
	// 전국 재고 흐름 분석
	InventoryAlertDTO analyzeNationwideInventoryTrend(String productCode);

	// 지점별 재고 흐름 분석
	InventoryAlertDTO analyzeBranchInventoryTrend(String productCode, String branchName);
}
