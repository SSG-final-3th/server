package com.exam.product;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, String> {
	List<Product> findByCategory(String category);

	List<Product> findByProductNameContaining(String productName);  // 상품 이름으로 검색

	// 🛠 상품 코드로 찾는 메서드 추가!
	Product findByProductCode(String productCode);

	List<Product> findByCategoryIn(List<String> categories);

	// userId를 기준으로 LikeCategory와 Product를 JOIN
	@Query("SELECT p FROM Product p JOIN Likecategories lc ON p.category = lc.category WHERE lc.userId = :userId")
	List<Product> findProductsByUserId(@Param("userId") String userId);

	//가격 높은순, 낮은순
	List<Product> findAllByOrderByPriceAsc();

	List<Product> findAllByOrderByPriceDesc();

	//elastic search 용

	@Query("SELECT new com.exam.product.ProductDTO(p.productCode, p.productName, p.category, p.price) FROM Product p")
	List<ProductDTO> findAllProducts();
	// 다중 제품 코드로 제품 찾기
	List<Product> findAllByProductCodeIn(List<String> productCodes);

	;

}