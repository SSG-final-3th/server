package com.exam.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exam.product.ProductDTO;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {


		/*
			관리자가
			상품 코드 등록(추가)/수정/삭제, - product 테이블 사용
			개별 상품 등록(추가)/수정/삭제, - goods 테이블 사용
			행사 등록(추가)/수정/삭제, - 새로 테이블 생성해야함... 행사 테이블
			지점 등록(추가)/수정/삭제 기능 구현해야 함 - branch 테이블 사용
		*/

	AdminService adminService;
	GoodsService goodsService;

	public AdminController(AdminService adminService, GoodsService goodsService) {
		this.adminService = adminService;
		this.goodsService = goodsService;
	}

	//  특정 상품 조회
	@GetMapping("/findByProductCode/{productCode}")
	public ResponseEntity<ProductDTO> findByProductCode(@PathVariable String productCode) {
		ProductDTO product = adminService.findByProductCode(productCode);
		return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
	}

	//상품코드 + 개별상품 전체 보기
	@GetMapping("/findAllProductCode")
	public ResponseEntity<List<ProductDTO>> findALlProductCode() {
		List<ProductDTO> productDTOList = adminService.findAllProducts();
		return ResponseEntity.status(200).body(productDTOList);
	}

	@GetMapping("/findAllGoods")
	public ResponseEntity<List<GoodsDTO>> findAllGoods() {
		List<GoodsDTO> goodsDTOList = adminService.findAllGoods();
		return ResponseEntity.status(200).body(goodsDTOList);
	}

	@PostMapping("/addProductCode")
	public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO) {
		adminService.addProduct(productDTO);
		return ResponseEntity.created(null).body(productDTO);  // 201 상태코드 반환됨.
	}

	@DeleteMapping("/deleteProductCode/{productCode}")
	public ResponseEntity<ProductDTO> deleteProduct(@PathVariable String productCode) {
		adminService.deleteProduct(productCode);
		return ResponseEntity.ok().build();  // 상태 코드 200 OK
	}

	@PutMapping("/updateProductCode/{productCode}")
	public ResponseEntity<ProductDTO> updateProduct(@PathVariable String productCode,
		@RequestBody ProductDTO productDTO) {
		adminService.updateProduct(productCode, productDTO);
		return ResponseEntity.status(201).body(productDTO); //201
	}

	@PostMapping("/addGoods")
	public ResponseEntity<GoodsDTO> addGoods(@Valid @RequestBody GoodsDTO goodsDTO) {
		adminService.addGoods(goodsDTO);
		return ResponseEntity.created(null).body(goodsDTO);  // 201 상태코드 반환됨.
	}

	@DeleteMapping("/deleteGoods/{goodsId}")
	public ResponseEntity<GoodsDTO> deleteGoods(@PathVariable Integer goodsId) {
		adminService.deleteGoods(goodsId);
		return ResponseEntity.ok().build();  // 상태 코드 200 OK
	}

	@PutMapping("/updateGoods/{goodsId}")
	public ResponseEntity<GoodsDTO> updateGoods(@PathVariable Integer goodsId,
		@RequestBody GoodsDTO goodsDTO) {
		adminService.updateGoods(goodsId, goodsDTO);
		return ResponseEntity.status(201).body(goodsDTO); //201
	}

	@DeleteMapping("/consume")
	public ResponseEntity<String> consumeGoods(
		@RequestParam String productCode,
		@RequestParam String branchName,
		@RequestParam int quantity) {

		goodsService.deleteGoodsByQuantity(productCode, branchName, quantity);
		return ResponseEntity.ok(quantity + "개 상품이 유통기한 순으로 차감되었습니다.");
	}

}
