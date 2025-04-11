package com.exam.inventory;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long logId;

	@Column(nullable = false)
	private String productCode;

	@Column(nullable = false)
	private String branchName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChangeType changeType; // IN or OUT

	@Column(nullable = false)
	private int quantity;

	@Column // nullable 허용할거임
	private Integer remainingStock;
	@Builder.Default

	@Column(nullable = false)
	private LocalDateTime changeDate = LocalDateTime.now();
}
