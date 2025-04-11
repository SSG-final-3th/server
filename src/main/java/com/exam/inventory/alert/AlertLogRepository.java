package com.exam.inventory.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {

	List<AlertLog> findByProductCodeOrderByAlertTimeDesc(String productCode);

	List<AlertLog> findAllByOrderByAlertTimeDesc();
}