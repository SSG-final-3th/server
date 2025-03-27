package com.exam.adminbranch;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

@Service // 비즈니스 로직 애노테이션
public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository; // 데이터 접근 로직을 위한 리포지토리

	// 생성자를 통한 의존성 주입
	public BranchServiceImpl(BranchRepository branchRepository) {
		this.branchRepository = branchRepository;
	}


	//모든 지점 조회
	@Override
	public List<Branch> getAllBranches() {
		// 데이터베이스에서 모든 지점을 조회
		return branchRepository.findAll();
	}

	//지점 생성
	@Override
	public Branch createBranches(Branch branch) {
		//Null 체크 Optional 로직
		Optional<Branch> existingBranch = branchRepository.findById(branch.getBranchName());
		if (existingBranch.isPresent()){
			throw new RuntimeException("지점 이름이 이미 존재합니다: " + branch.getBranchName());
		}
		return branchRepository.save(branch);
	}

	//지점 수정
	@Override
	@Transactional //트랜젝션 자동 관리 애노테이션 로직이 종료되면 자동 Commit
	public Branch updateBranches(Branch branch) {
		Branch existingBranch = branchRepository.findById(branch.getBranchName())
			.orElseThrow(() -> new BranchNotFoundException("지점을 찾을 수 없습니다.: " + branch.getBranchName()));

		existingBranch.setBranchAddress(branch.getBranchAddress());
		existingBranch.setBranchName(branch.getBranchName());
		return branchRepository.save(existingBranch);

	}

	//지점 삭제
	@Override
	@Transactional
	public void deleteBranches(Branch branch, String branchName) {
		Branch existingBranch = branchRepository.findById(branchName)
			.orElseThrow(() -> new BranchNotFoundException("지점을 찾을 수 없습니다: " + branchName));

		branchRepository.delete(existingBranch);
	}


	//커스텀 예외(지점 수정에서 사용 중)
	public class BranchNotFoundException extends RuntimeException {
		public BranchNotFoundException(String message) {
			super(message);
		}
	}
}