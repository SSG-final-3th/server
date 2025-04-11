package com.exam.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	//  전체 상품 목록 조회
	@GetMapping("/home")
	public ResponseEntity<List<ProductDTO>> getAllProducts() {
		List<ProductDTO> products = productService.getAllProducts();
		return ResponseEntity.ok(products);
	}

	//  특정 상품 상세 조회
	@GetMapping("/detail/{productCode}")
	public ResponseEntity<ProductDTO> getProductDetail(@PathVariable String productCode) {
		ProductDTO product = productService.getProductByCode(productCode);
		return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
	}

	//카테고리별 상품 조회
	@GetMapping("/{category}")
	public ResponseEntity<List<ProductDTO>> getCategoryProducts(@PathVariable String category) {
		List<ProductDTO> products = productService.getProductsByCategory(category);
		return ResponseEntity.ok(products);
	}

	@GetMapping("/search/{productName}")
	public ResponseEntity<List<ProductDTO>> searchProductsByName(@PathVariable String productName) {
		List<ProductDTO> products = productService.getProductsByName(productName);
		return (products.isEmpty()) ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
	}

	@GetMapping("/likecategories")
	public ResponseEntity<?> getAllProductsByCategory() {
		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();

		String userId = authentication.getName();
		if (userId == null) {
			return ResponseEntity.status(401).body("인증되지 않은 사용자");
		}
		System.out.println(userId);

		List<ProductDTO> productDTOs = productService.getProductsByUserId(userId);
		return ResponseEntity.ok(productDTOs);
	}

	@GetMapping("/search")
	public ResponseEntity<List<ProductDTO>> searchProducts(
		@RequestParam String searchType,
		@RequestParam String keyword) {

		List<ProductDTO> products;

		if ("productName".equals(searchType)) {
			// 기존의 상품명으로 검색하는 서비스 메소드 활용
			products = productService.getProductsByName(keyword);
		} else if ("category".equals(searchType)) {
			// 기존의 카테고리별 상품 조회 서비스 메소드 활용
			products = productService.getProductsByCategory(keyword);
		} else {
			// 유효하지 않은 검색 타입인 경우
			return ResponseEntity.badRequest().build();
		}

		return (products.isEmpty()) ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
	}

	@GetMapping
	public ResponseEntity<List<Product>> getProducts(@RequestParam(defaultValue = "default") String sort) {
		List<Product> products = productService.getProductsSorted(sort);
		return ResponseEntity.ok(products);
	}

}
