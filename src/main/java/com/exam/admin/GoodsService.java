package com.exam.admin;

import java.time.LocalDateTime;

public interface GoodsService {
	void deleteExpiredGoods(LocalDateTime now);

	void deleteGoodsByQuantity(String productCode, String branchName, int quantity);
}
