package com.exam.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exam.review.ReviewRepository;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ReviewRepository reviewRepository;

	public ProductServiceImpl(ProductRepository productRepository, ReviewRepository reviewRepository) {
		this.productRepository = productRepository;
		this.reviewRepository = reviewRepository;
	}

	// 전체 상품 조회하기
	@Override
	public List<ProductDTO> getAllProducts() {
		List<Product> products = productRepository.findAll();

		return products.stream().map(product -> {
			double avgRating = reviewRepository.getAverageRating(product.getProductCode()); // 평균 별점 가져오기
			return convertToDTO(product, avgRating);
		}).collect(Collectors.toList());
	}

	// 상품 하나 조회하기
	@Override
	public ProductDTO getProductByCode(String productCode) {
		Product product = productRepository.findById(productCode).orElse(null);
		if (product == null) {
			return null;
		}
		double avgRating = reviewRepository.getAverageRating(productCode); // ⭐ 평균 별점 가져오기
		return convertToDTO(product, avgRating);
	}

	@Override
	public List<ProductDTO> getProductsByCategory(String category) {
		List<Product> products = productRepository.findByCategory(category);

		return products.stream().map(product -> {
			double avgRating = reviewRepository.getAverageRating(product.getProductCode()); // 평균 별점 가져오기
			return convertToDTO(product, avgRating);
		}).collect(Collectors.toList());
	}

	// entity에서 dto로 만들기
	private ProductDTO convertToDTO(Product product, double avgRating) {
		return ProductDTO.builder()
			.productCode(product.getProductCode())
			.category(product.getCategory())
			.productName(product.getProductName())
			.description(product.getDescription())
			.price(product.getPrice())
			.image(product.getImage())
			.averageRating(avgRating) // 별점 추가 (이건 db에는 없는 거임)
			.build();
	}

	@Override
	public List<ProductDTO> getProductsByName(String productName) {
		// 상품 이름으로 검색 로직
		List<Product> products = productRepository.findByProductNameContaining(productName);

		return products.stream().map(product -> {
			double avgRating = reviewRepository.getAverageRating(product.getProductCode()); // 평균 별점 가져오기
			return convertToDTO(product, avgRating);
		}).collect(Collectors.toList());
	}
}
