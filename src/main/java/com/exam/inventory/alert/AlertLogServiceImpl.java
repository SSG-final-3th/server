package com.exam.inventory.alert;

import com.exam.inventory.ai.InventoryAlertDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertLogServiceImpl implements AlertLogService {

	private final AlertLogRepository alertLogRepository;

	@Override
	public void saveAlert(String productCode, String branchName, InventoryAlertDTO dto) {
		AlertLog log = AlertLog.builder()
			.productCode(productCode)
			.branchName(branchName)
			.anomaly(dto.isAnomaly())
			.trendSummary(dto.getTrendSummary())
			.recommendation(dto.getRecommendation())
			.riskScore(dto.getRiskScore())
			.alertTime(LocalDateTime.now())
			.build();

		alertLogRepository.save(log);
	}

	@Override
	public List<AlertLog> getAllAlerts() {
		return alertLogRepository.findAllByOrderByAlertTimeDesc();
	}

	@Override
	public List<AlertLog> getAlertsByProduct(String productCode) {
		return alertLogRepository.findByProductCodeOrderByAlertTimeDesc(productCode);
	}

	@Override
	public void markAlertAsRead(Long alertId) {
		AlertLog alert = alertLogRepository.findById(alertId)
			.orElseThrow(() -> new RuntimeException("해당 알림을 찾을 수 없습니다."));

		alert.setAlertRead(true);
		alertLogRepository.save(alert);
	}
}