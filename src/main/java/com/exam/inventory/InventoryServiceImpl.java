package com.exam.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

	InventoryRepository inventoryRepository;
	InventoryLogRepository inventoryLogRepository;

	public InventoryServiceImpl(InventoryRepository inventoryRepository, InventoryLogRepository inventoryLogRepository) {
		this.inventoryRepository = inventoryRepository;
		this.inventoryLogRepository = inventoryLogRepository;
	}

	@Override
	public InventoryDTO findByProductCodeAndBranchName(String productCode, String branchName) {
		Inventory inventory = inventoryRepository.findByProductCodeAndBranchName(productCode, branchName);

		//Inventory -> InventoryDTO
		InventoryDTO inventoryDTO = InventoryDTO.builder()
			.productCode(inventory.getProductCode())
			.quantity(inventory.getQuantity())
			.branchName(inventory.getBranchName())
			.build();

		return inventoryDTO;
	}

	@Override
	public List<InventoryDTO> findAllInventory() {
		List<Inventory> inventoryList = inventoryRepository.findAll();
		//Stream API 의 Map 이용
		List<InventoryDTO> inventoryDTOList =
			inventoryList.stream().map(i -> {
				InventoryDTO dto = InventoryDTO.builder()
					.productCode(i.getProductCode())
					.quantity(i.getQuantity())
					.branchName(i.getBranchName())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return inventoryDTOList;
	}

	@Override
	public List<InventoryDTO> findByProductCode(String productCode) {
		List<Inventory> inventoryList = inventoryRepository.findByProductCode(productCode);
		List<InventoryDTO> inventoryDTOList =
			inventoryList.stream().map(i -> {
				InventoryDTO dto = InventoryDTO.builder()
					.productCode(i.getProductCode())
					.quantity(i.getQuantity())
					.branchName(i.getBranchName())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return inventoryDTOList;
	}

	// 특정 상품의 지점별 수량 조회 메소드
	@Override
	public Map<String, Integer> findQuantityByProductCode(String productCode) {
		List<Inventory> inventories = inventoryRepository.findByProductCode(productCode);
		Map<String, Integer> branchQuantities = new HashMap<>();

		for (Inventory inventory : inventories) {
			branchQuantities.put(inventory.getBranchName(), inventory.getQuantity());
		}

		return branchQuantities;
	}

	@Override
	@Transactional
	public boolean updateInventory(String branchName, String productCode, int quantityChange) {
		try {
			Inventory inventory = inventoryRepository.findByProductCodeAndBranchName(productCode, branchName);

			if (inventory == null) {
				return false;
			}

			int currentQuantity = inventory.getQuantity();
			int newQuantity = currentQuantity + quantityChange;

			// 재고가 0 미만이 되지 않도록 체크
			if (newQuantity < 0) {
				return false;
			}

			inventory.setQuantity(newQuantity);
			inventoryRepository.save(inventory);

			//  [추가] 재고 로그 저장
			ChangeType changeType = quantityChange >= 0 ? ChangeType.IN : ChangeType.OUT;

			InventoryLog log = InventoryLog.builder()
				.productCode(productCode)
				.branchName(branchName)
				.changeType(changeType)
				.quantity(Math.abs(quantityChange)) // 무조건 절댓값으로
				.remainingStock(newQuantity)
				.changeDate(java.time.LocalDateTime.now())
				.build();

			inventoryLogRepository.save(log);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
