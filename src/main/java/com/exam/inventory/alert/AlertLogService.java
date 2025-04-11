package com.exam.inventory.alert;

import com.exam.inventory.ai.InventoryAlertDTO;

import java.util.List;

public interface AlertLogService {

	void saveAlert(String productCode, String branchName, InventoryAlertDTO dto);

	List<AlertLog> getAllAlerts();

	List<AlertLog> getAlertsByProduct(String productCode);

	void markAlertAsRead(Long alertId);

}
