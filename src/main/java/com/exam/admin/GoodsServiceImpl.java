package com.exam.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exam.inventory.Inventory;
import com.exam.inventory.InventoryRepository;

import jakarta.transaction.Transactional;

@Service
public class GoodsServiceImpl implements GoodsService {

	AdminRepositoryGoods adminRepositoryGoods;
	InventoryRepository inventoryRepository;

	public GoodsServiceImpl(AdminRepositoryGoods adminRepositoryGoods, InventoryRepository inventoryRepository) {
		this.adminRepositoryGoods = adminRepositoryGoods;
		this.inventoryRepository = inventoryRepository;
	}

	@Override
	@Transactional
	public void deleteExpiredGoods(LocalDateTime now) {
		// 1. 유통기한이 현재 시간에서 1일 이내로 남은 상품을 가져옵니다.
		LocalDateTime oneDayLater = now.plusDays(1);

		List<Goods> nearExpiredGoods = adminRepositoryGoods.findByExpirationDateBefore(
				oneDayLater) // 유통기한이 현재로부터 1일 이내로 남은 상품
			.stream()
			.filter(goods -> goods.getExpirationDate().isBefore(now)  // 유통기한이 과거인 상품
				|| (goods.getExpirationDate().isAfter(now) && goods.getExpirationDate()
				.isBefore(oneDayLater))) // 현재부터 1일 이내로 남은 상품
			.collect(Collectors.toList());

		// 2. 유통기한이 과거 또는 현재부터 1일 이내로 남은 상품들을 삭제합니다.
		adminRepositoryGoods.deleteAll(nearExpiredGoods); // 유통기한이 과거이거나 1일 이내로 남은 상품들 삭제

		// 3. 유통기한이 과거이거나 현재부터 1일 이내로 남은 상품들에 대해 처리
		for (Goods goods : nearExpiredGoods) {
			// 4. Inventory 테이블에서 해당 상품과 관련된 레코드를 찾습니다.
			Inventory inventory = inventoryRepository.findByProductCodeAndBranchName(goods.getProductCode(),
				goods.getBranchName());

			// 5. 해당 상품이 Inventory에 있다면 quantity 감소 또는 삭제 처리
			if (inventory != null) {
				if (inventory.getQuantity() > 1) {
					// quantity가 1 이상이면 1 감소
					inventory.setQuantity(inventory.getQuantity() - 1);
					inventoryRepository.save(inventory);  // 갱신된 정보 저장
				} else {
					// quantity가 1 이하이면 해당 레코드 삭제
					inventoryRepository.delete(inventory);  // 해당 레코드 삭제
				}
			}
		}
	}

	@Override
	public void deleteGoodsByQuantity(String productCode, String branchName, int quantity) {
		adminRepositoryGoods.deleteOldestGoods(productCode, branchName, quantity);
	}

}

