package com.exam.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exam.adminbranch.Branch;
import com.exam.adminbranch.BranchRepository;
import com.exam.inventory.ChangeType;
import com.exam.inventory.Inventory;
import com.exam.inventory.InventoryLog;
import com.exam.inventory.InventoryLogRepository;
import com.exam.inventory.InventoryRepository;
import com.exam.product.Product;
import com.exam.product.ProductDTO;
import com.exam.product.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

	AdminRepositoryGoods adminRepositoryGoods;
	AdminRepositoryProducts adminRepositoryProducts;
	InventoryRepository inventoryRepository;
	BranchRepository branchRepository;
	ProductRepository productRepository;
	InventoryLogRepository inventoryLogRepository;

	public AdminServiceImpl(AdminRepositoryGoods adminRepositoryGoods,
		AdminRepositoryProducts adminRepositoryProducts,
		InventoryRepository inventoryRepository,
		BranchRepository branchRepository,
		ProductRepository productRepository,
		InventoryLogRepository inventoryLogRepository) {
		this.adminRepositoryGoods = adminRepositoryGoods;
		this.adminRepositoryProducts = adminRepositoryProducts;
		this.inventoryRepository = inventoryRepository;
		this.branchRepository = branchRepository;
		this.productRepository = productRepository;
		this.inventoryLogRepository = inventoryLogRepository;
	}

	@Override
	public void addGoods(GoodsDTO goodsDTO) {
		// 1. 지점이 존재하는지 확인
		Optional<Branch> branch = branchRepository.findByBranchName(goodsDTO.getBranchName());

		// 지점이 존재하지 않으면, 외래키 제약 조건 오류 발생
		if (!branch.isPresent()) {
			throw new RuntimeException("존재하지 않는 지점명입니다. 지점명을 확인해주세요: " + goodsDTO.getBranchName());
		}

		// 2. Goods를 quantity 개수만큼 반복하여 추가
		for (int i = 0; i < goodsDTO.getQuantity(); i++) {
			Goods goods = Goods.builder()
				.productCode(goodsDTO.getProductCode())
				.branchName(goodsDTO.getBranchName())
				.expirationDate(goodsDTO.getExpirationDate())
				.build();
			adminRepositoryGoods.save(goods);  // 상품 정보를 DB에 저장
		}

		// 3. Inventory 테이블에서 해당 productCode와 branchName을 가진 레코드 확인
		Inventory inventory = inventoryRepository.findByProductCodeAndBranchName(
			goodsDTO.getProductCode(), goodsDTO.getBranchName());

		// 4. 해당하는 Inventory 레코드가 없다면 새로 생성
		if (inventory == null) {
			Inventory newInventory = Inventory.builder()
				.productCode(goodsDTO.getProductCode())
				.branchName(goodsDTO.getBranchName())
				.quantity(goodsDTO.getQuantity())  // 입고 개수만큼 초기화
				.build();
			inventoryRepository.save(newInventory);

			// 새로 생성된 재고 정보 저장
		} else {
			// 5. 해당하는 Inventory 레코드가 있다면 수량을 입고 개수만큼 증가
			inventory.setQuantity(inventory.getQuantity() + goodsDTO.getQuantity());  // 입고 개수만큼 수량 증가
			inventoryRepository.save(inventory);  // 갱신된 정보 저장
		}
		// 현재까지의 누적 재고량 계산
		List<InventoryLog> logs = inventoryLogRepository
			.findByProductCodeAndBranchNameOrderByChangeDateAsc(
				goodsDTO.getProductCode(), goodsDTO.getBranchName());

		int currentStock = 0;
		for (InventoryLog log : logs) {
			currentStock += (log.getChangeType() == ChangeType.IN) ? log.getQuantity() : -log.getQuantity();
		}

		int updatedStock = currentStock + goodsDTO.getQuantity(); // 입고이므로 더하기
		// 상품 입고 되는거 로그테이블에 저장
		inventoryLogRepository.save(
			InventoryLog.builder()
				.productCode(goodsDTO.getProductCode())
				.branchName(goodsDTO.getBranchName())
				.changeType(ChangeType.IN)
				.remainingStock(updatedStock)
				.quantity(goodsDTO.getQuantity())
				.changeDate(LocalDateTime.now())
				.build());
	}

	@Transactional
	public void deleteGoods(List<Integer> goodsIds) {
		List<Goods> goodsList = adminRepositoryGoods.findAllById(goodsIds);

		// 1. 그룹핑 (productCode + branchName 기준)
		Map<String, List<Goods>> grouped = goodsList.stream()
			.collect(Collectors.groupingBy(g -> g.getProductCode() + "_" + g.getBranchName()));

		for (Map.Entry<String, List<Goods>> entry : grouped.entrySet()) {
			List<Goods> group = entry.getValue();
			String productCode = group.get(0).getProductCode();
			String branchName = group.get(0).getBranchName();
			int deleteCount = group.size();

			// 2. 삭제는 여전히 개별로 수행
			for (Goods goods : group) {
				adminRepositoryGoods.delete(goods);
			}

			// 3. 재고 감소
			Inventory inventory = inventoryRepository.findByProductCodeAndBranchName(productCode, branchName);
			if (inventory != null) {
				int newQty = inventory.getQuantity() - deleteCount;

				if (newQty > 0) {
					inventory.setQuantity(newQty);
					inventoryRepository.save(inventory);
				} else {
					inventoryRepository.delete(inventory);
					newQty = 0; // 로그 용
				}

				//  로그 저장
				inventoryLogRepository.save(
					InventoryLog.builder()
						.productCode(productCode)
						.branchName(branchName)
						.changeType(ChangeType.OUT)
						.quantity(deleteCount)
						.remainingStock(newQty)
						.changeDate(LocalDateTime.now())
						.build()
				);
			}
		}
	}

	@Override
	@Transactional
	public void updateGoods(int goodsId, GoodsDTO goodsDTO) {

		Goods goods = adminRepositoryGoods.findByGoodsId(goodsId).orElse(null);
		//ProductCode 찾으면 product로 리턴, 없으면 null 리턴

		if (goods != null) {
			goods.setProductCode(goodsDTO.getProductCode());
			goods.setBranchName(goodsDTO.getBranchName());
			goods.setExpirationDate(goodsDTO.getExpirationDate());
		}

	}

	@Override
	public ProductDTO findByProductCode(String productCode) {
		Product product = productRepository.findById(productCode).orElse(null);
		if (product == null) {
			return null;
		}
		return convertToDTO(product);
	}

	@Override
	public List<ProductDTO> findAllProducts() {
		List<Product> productList = adminRepositoryProducts.findAll();

		//Stream API 의 Map 이용
		List<ProductDTO> productDTOList =
			productList.stream().map(t -> { //t는 Todo
				ProductDTO dto = ProductDTO.builder()
					.productCode(t.getProductCode())
					.category(t.getCategory())
					.productName(t.getProductName())
					.description(t.getDescription())
					.price(t.getPrice())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return productDTOList;
	}

	@Override
	public List<GoodsDTO> findAllGoods() {
		List<Goods> goodsList = adminRepositoryGoods.findAll();

		//Stream API 의 Map 이용
		List<GoodsDTO> goodsDTOList =
			goodsList.stream().map(t -> { //t는 Todo
				GoodsDTO dto = GoodsDTO.builder()
					.goodsId(t.getGoodsId())
					.productCode(t.getProductCode())
					.branchName(t.getBranchName())
					.expirationDate(t.getExpirationDate())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return goodsDTOList;
	}

	@Override
	@Transactional
	public void addProduct(ProductDTO productDTO) {

		// 1. productCode가 존재하는지 확인
		Optional<Product> dto = productRepository.findById(productDTO.getProductCode());

		// productCode가 존재하면, primary key 제약 조건 오류 발생
		if (dto.isPresent()) {
			throw new RuntimeException("이미 존재하는 productCode 입니다. 다른 productCode 입력해주세요." + productDTO.getProductCode());
		}

		Product product = Product.builder()
			.productCode(productDTO.getProductCode())
			.category(productDTO.getCategory())
			.productName(productDTO.getProductName())
			.description(productDTO.getDescription())
			.price(productDTO.getPrice())
			.image(productDTO.getImage())
			.build();

		adminRepositoryProducts.save(product);
	}

	@Override
	@Transactional
	public void deleteProduct(String productCode) {

		Product product = adminRepositoryProducts.findByProductCode(productCode).orElse(null);
		System.out.println("product : " + product);
		//productCode 찾으면 product 리턴, 없으면 null 리턴

		if (product != null) {
			adminRepositoryProducts.delete(product);
			// adminRepositoryProducts.deleteById(product.getId());
		}
	}

	@Override
	@Transactional
	public void updateProduct(String productCode, ProductDTO productDTO) {
		//작업 순서
		/*
		  1. id에 해당되는 엔티티 찾기
		  2. 찾은 엔티티를 dto 값으로 수정
		  3. 더티체킹으로 인해서 자동으로 수정
		  	 또는 명시적으로 save(엔티티) 호출도 가능
		 */

		Product product = adminRepositoryProducts.findByProductCode(productCode).orElse(null);
		//ProductCode 찾으면 product로 리턴, 없으면 null 리턴

		if (product != null) {
			product.setCategory(productDTO.getCategory());
			product.setProductName(productDTO.getProductName());
			product.setDescription(productDTO.getDescription());
			product.setPrice(productDTO.getPrice());
			product.setImage(productDTO.getImage());
		}

	}

	// entity에서 dto로 만들기
	private ProductDTO convertToDTO(Product product) {
		return ProductDTO.builder()
			.productCode(product.getProductCode())
			.category(product.getCategory())
			.productName(product.getProductName())
			.description(product.getDescription())
			.price(product.getPrice())
			.image(product.getImage())
			.build();
	}

}



