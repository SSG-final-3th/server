package com.exam.inventory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

	// 지점 + 상품번호로 조회
	List<InventoryLog> findByProductCodeAndBranchNameOrderByChangeDateAsc(String productCode, String branchName);

	// 상품 번호로만 조회
	List<InventoryLog> findByProductCodeOrderByChangeDateAsc(String productCode);

	// 마이그레이션용
	List<InventoryLog> findAllByOrderByChangeDateAsc();

	// OPEN AI API를 위한 전국 지표 분석
	List<InventoryLog> findByProductCodeOrderByChangeDate(String productCode);

	// OPEN AI API를 위한 지점별 지표 분석
	@Query("SELECT DISTINCT new com.exam.inventory.ProductBranchKey(i.productCode, i.branchName) FROM InventoryLog i")
	List<ProductBranchKey> findAllProductBranchPairs();

}