package com.exam.inventory.alert;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertlog")
@RequiredArgsConstructor
public class AlertLogController {

	private final AlertLogService alertLogService;

	// 전체 알림 조회
	@GetMapping
	public ResponseEntity<List<AlertLog>> getAllAlerts() {
		return ResponseEntity.ok(alertLogService.getAllAlerts());
	}

	// 상품별 알림 조회
	@GetMapping("/{productCode}")
	public ResponseEntity<List<AlertLog>> getAlertsByProduct(@PathVariable String productCode) {
		return ResponseEntity.ok(alertLogService.getAlertsByProduct(productCode));
	}

	// 알림 읽음 처리 용도
	@PutMapping("/read/{alertId}")
	public ResponseEntity<Void> markAlertAsRead(@PathVariable Long alertId) {
		alertLogService.markAlertAsRead(alertId);
		return ResponseEntity.ok().build();
	}
}

