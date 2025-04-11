package com.exam.inventory.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// AI 분석의 결과를 담는 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAlertDTO {
	private boolean anomaly;
	private String trendSummary;
	private String recommendation;
	private int riskScore;
}
