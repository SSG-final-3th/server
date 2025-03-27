// 📌 주문 정보에서 특정 유저가 특정 상품을 구매한 적이 있는지 확인하는 메서드 추가
package com.exam.orderinfo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.exam.order.OrderInfo;

public interface OrderInfoRepository extends JpaRepository<com.exam.order.OrderInfo, Long> {
	// 댓글쓸떄 구매내역 조회
	boolean existsByUserIdAndProductCode(String userId, String productCode);

	List<OrderInfo> findByImpUid(String impUid);

	List<OrderInfo> findByUserId(String userId);
}
