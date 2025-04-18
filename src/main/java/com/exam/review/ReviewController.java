package com.exam.review;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	//  리뷰 작성
	@PostMapping("/add")
	public ResponseEntity<?> addReview(@RequestBody ReviewDTO dto) {
		try {
			String userId = getAuthenticatedUserId(); // 인증된 사용자 ID 가져오기
			reviewService.addReview(dto, userId);
			return ResponseEntity.status(HttpStatus.CREATED).body("리뷰가 성공적으로 등록되었습니다.");
		} catch (IllegalArgumentException e) {
			// 400 Bad Request 반환
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			// 500 Internal Server Error 반환
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
		}
	}

	//  리뷰 수정
	@PutMapping("/update/{reviewId}")
	public ResponseEntity<String> updateReview(@PathVariable Long reviewId, @RequestBody ReviewDTO dto) {
		String userId = getAuthenticatedUserId();
		reviewService.updateReview(reviewId, dto, userId);
		return ResponseEntity.ok("리뷰 수정 완료");
	}

	//  리뷰 삭제
	@DeleteMapping("/delete/{reviewId}")
	public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
		String userId = getAuthenticatedUserId();
		reviewService.deleteReview(reviewId, userId);
		return ResponseEntity.ok("리뷰 삭제 완료");
	}

	// 특정 상품만의 리뷰 조회
	@GetMapping("/product/{productCode}")
	public ResponseEntity<List<ReviewDTO>> getReviewsByProduct(@PathVariable String productCode) {
		List<ReviewDTO> reviews = reviewService.getReviewsByProduct(productCode);
		return ResponseEntity.ok(reviews);
	}

	// 특정 사용자가 쓴 리뷰만 조회
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ReviewDTO>> getReviewsByUser(@PathVariable String userId) {

		String authenticatedUserId = getAuthenticatedUserId();
		if (!authenticatedUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		List<ReviewDTO> reviews = reviewService.getReviewsByUser(userId);
		return ResponseEntity.ok(reviews);
	}

	// 특정 상품의 별점 별 리뷰 조회
	@GetMapping("/{productCode}/{rating}")
	public ResponseEntity<?> getReviewsByRating(@PathVariable String productCode,
		@PathVariable int rating) {
		List<ReviewDTO> reviews = reviewService.findByProductCodeAndRating(productCode, rating);
		if (reviews.isEmpty()) {
			// 리뷰가 없으면 "해당 별점 리뷰가 없습니다" 메시지와 함께 반환
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 별점 리뷰가 없습니다");
		}
		// 리뷰가 있으면 리뷰 목록을 반환
		return ResponseEntity.ok(reviews);
	}

	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
}
