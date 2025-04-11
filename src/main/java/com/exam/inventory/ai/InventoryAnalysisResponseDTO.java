package com.exam.inventory.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 응답 결과를 프론트엔드에게 반활할때 쓰는 DTO
// Alert DTO를 감싸는데 왜 하나의 DTO 안쓰냐면 재사용성을 위해서
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAnalysisResponseDTO {
	private String productCode;
	private String branchName;
	private InventoryAlertDTO aiAnalysis; // AI 분석 결과
}