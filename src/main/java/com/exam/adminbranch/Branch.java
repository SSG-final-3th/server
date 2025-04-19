package com.exam.adminbranch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "branch")
public class Branch {
	@Id
	@Column(nullable = false)
	String branchName;

	@Column(nullable = false)
	String branchAddress;

	//엔티티에 위도 경도 필드 추가
	@Column
	Double latitude;

	@Column
	Double longitude;


}
