package com.exam.orderinfo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderInfoController {

	private final OrderInfoService orderInfoService;

	public OrderInfoController(OrderInfoService orderInfoService) {
		this.orderInfoService = orderInfoService;
	}

	// 특정 사용자 주문 내역 조회
	@GetMapping("/myorder")
	public ResponseEntity<List<OrderInfoDTO>> getMyOrders() {
		String userId = getAuthenticatedUserId(); // JWT에서 userId 가져오기
		List<OrderInfoDTO> orders = orderInfoService.getOrdersByUserId(userId);
		return ResponseEntity.ok(orders);
	}

	@PostMapping("/confirm")
	public ResponseEntity<?> confirmOrder(@RequestBody OrderInfoDTO orderDto) {
		String userId = getAuthenticatedUserId();

		// userId 강제 세팅 (보안상 클라이언트에서 전달된 userId는 무시) --> ?
		orderDto.setUserId(userId);

		try {
			// 1. 포트원 결제 검증
			boolean isValid = orderInfoService.verifyAndSaveOrder(orderDto);

			if (!isValid) {
				return ResponseEntity.badRequest().body("결제 검증 실패 또는 금액 불일치");
			}

			return ResponseEntity.ok("주문이 성공적으로 처리되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
		}
	}

	@GetMapping("/complete/{impUid}")
	public ResponseEntity<List<OrderInfoDTO>> getOrderInfoByImpUid(@PathVariable String impUid) {
		List<OrderInfoDTO> orderInfoList = orderInfoService.getOrderInfoByImpUid(impUid);
		if (orderInfoList == null || orderInfoList.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(orderInfoList);
	}





	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
}
