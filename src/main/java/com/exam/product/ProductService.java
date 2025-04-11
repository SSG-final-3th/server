package com.exam.product;

import java.util.List;

public interface ProductService {
	List<ProductDTO> getAllProducts(); // 전체 상품 조회

	ProductDTO getProductByCode(String productCode); // 특정 상품 조회

	List<ProductDTO> getProductsByCategory(String category);

	List<ProductDTO> getProductsByName(String productName); // 상품 이름으로 검색

	List<ProductDTO> getProductsByUserId(String userId); //userid로 선호 category별 상품 조회

	List<Product> getProductsSorted(String sort);
}
