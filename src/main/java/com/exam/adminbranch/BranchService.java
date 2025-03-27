package com.exam.adminbranch;

import java.util.List;

public interface BranchService {
	//모든 지점 조회
	List<Branch> getAllBranches();

	//지점 추가
	Branch createBranches(Branch branch);

	//지점 수정
	Branch updateBranches(Branch branch);

	//지점 삭제
	void deleteBranches(Branch branch, String branchName);
}
