package com.exam.adminbranch;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.exam.inventory.InventoryService;

@RestController
@RequestMapping("/admin/branch")
public class BranchController {

	@Autowired
	private final BranchService branchService;
	private final InventoryService inventoryService;

	public BranchController(BranchService branchService, InventoryService inventoryService) {
		this.branchService = branchService;
		this.inventoryService = inventoryService;
	}

	//모든 지점 조회
	@GetMapping("/all")
	public ResponseEntity<?> getAllBranches() {
		try {
			List<Branch> branches = branchService.getAllBranches();
			List<BranchDTO> branchDTO = branches.stream()
				.map(branch -> BranchDTO.builder()
					.branchName(branch.getBranchName())
					.branchAddress(branch.getBranchAddress())
					.latitude(branch.getLatitude())
					.longitude(branch.getLongitude())
					.build())
				.collect(Collectors.toList());

			return ResponseEntity.ok(branchDTO);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
		}
	}



	//지점 생성
	 @PostMapping("/create")
	 public ResponseEntity<?> createBranch(@RequestBody BranchDTO branchDTO){
		try {
			//1.빌더 패턴을 사용하여 객체 생성
			Branch branch = Branch.builder()
				.branchName(branchDTO.getBranchName())
				.branchAddress(branchDTO.getBranchAddress())
				.latitude(branchDTO.getLatitude())
				.longitude(branchDTO.getLongitude())
				.build();
			//2. 브랜치서비스의 비즈니스 로직 가져오기
			Branch createdBranch = branchService.createBranches(branch);

			//3. DTO를 통해서 클라이언트에게 반환
			BranchDTO responseDTO = BranchDTO.builder()
				.branchName(createdBranch.getBranchName())
				.branchAddress(createdBranch.getBranchAddress())
				.build();
			//4. 응답 엔티티를 통해서 상태코드 반환
			return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
		} catch(RuntimeException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(e.getMessage());
		} catch(Exception e){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 생성 중 오류가 발생했습니다: " + e.getMessage());
		}
	 }

	 //지점 수정
	 @PutMapping("update/{branchName}")
	public ResponseEntity<?> updateBranch(@PathVariable String branchName, @RequestBody BranchDTO branchDTO){
		try{
			Branch branch = Branch.builder()
				.branchName(branchDTO.getBranchName())
				.branchAddress(branchDTO.getBranchAddress())
				.build();

			Branch updateBranch = branchService.updateBranches(branch);

			BranchDTO responseDTO = BranchDTO.builder()
				.branchName(updateBranch.getBranchName())
				.branchAddress(updateBranch.getBranchAddress())
				.build();
			return ResponseEntity.ok(responseDTO);
		} catch (BranchServiceImpl.BranchNotFoundException e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 수정 중 오류가 발생했습니다: " + e.getMessage());
		}
	 }

	 //지점 삭제
	@DeleteMapping("delete/{branchName}")
	public ResponseEntity<?> deleteBranch(@PathVariable String branchName){
		try{
			branchService.deleteBranches(null, branchName);
			return ResponseEntity.ok("지점이 성공적으로 삭제되었습니다: " + branchName);
		} catch (BranchServiceImpl.BranchNotFoundException e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(e.getMessage());
		} catch (Exception e){
			return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 특정 상품에 대한 지점별 수량 조회
	@GetMapping("/product/{productCode}")
	public ResponseEntity<?> getBranchesWithProduct(@PathVariable String productCode) {
		try {
			List<BranchLocationDTO> branches = branchService.getBranchesWithProduct(productCode);
			return ResponseEntity.ok(branches);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("상품별 지점 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 가장 가까운 매장 찾기
	@PostMapping("nearest")
	public ResponseEntity<?> findNearestBranch(@RequestBody CoordinateDTO coordinateDTO){
		try{
			BranchDTO nearestBranch = branchService.findNearestBranch(
				coordinateDTO.getLatitude(),
				coordinateDTO.getLongitude()
			);
			return ResponseEntity.ok(nearestBranch);
		}catch (Exception e){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("가장 가까운 지점을 찾는 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	@PostMapping("/nearestWithStock")
	public ResponseEntity<?> findNearestBranchWithStock(@RequestBody NearestWithStockRequestDTO requestDTO) {
		try {
			// 요청에서 필요한 데이터 추출
			double latitude = requestDTO.getLatitude();
			double longitude = requestDTO.getLongitude();
			List<String> productCodes = requestDTO.getProductCodes();
			int limit = requestDTO.getLimit() != null ? requestDTO.getLimit() : 5; // 기본값 5개 지점 반환

			// 가까운 지점 찾기 + 재고 정보 확인 서비스 호출
			List<BranchWithStockDTO> nearestBranchesWithStock = branchService.findNearestBranchesWithStock(
				latitude, longitude, productCodes, limit
			);

			return ResponseEntity.ok(nearestBranchesWithStock);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("재고가 있는 가장 가까운 지점을 찾는 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

		//커스텀 예외(지점 수정에서 사용 중)
		public class BranchNotFoundException extends RuntimeException {
			public BranchNotFoundException(String message) {
				super(message);
			}
		}
	}

