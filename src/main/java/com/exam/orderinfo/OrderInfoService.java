package com.exam.orderinfo;

import java.util.List;

public interface OrderInfoService {
	List<OrderInfoDTO> getOrdersByUserId(String userId);

	boolean verifyAndSaveOrder(OrderInfoDTO orderDto);

	List<OrderInfoDTO> getOrderInfoByImpUid(String impUid);
}
