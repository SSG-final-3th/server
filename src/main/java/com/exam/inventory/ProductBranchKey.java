package com.exam.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;



@Setter
@Getter
@AllArgsConstructor
public class ProductBranchKey {
	private String productCode;
	private String branchName;
}
