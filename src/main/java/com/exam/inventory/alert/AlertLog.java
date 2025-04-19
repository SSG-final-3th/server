package com.exam.inventory.alert;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alertlog")
public class AlertLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long alertId;

	@Column(nullable = false)
	private String productCode;

	@Column(nullable = false)
	private String branchName;
	private boolean anomaly; // AI가 이상하면 TRUE , 정상적이면 FALSE 보낼거임
	private String trendSummary; // 재고 흐름 요약
	private String recommendation; // 관리자에게 추천하는 행위
	private int riskScore; // 위험 점수
	private boolean alertRead = false;
	private LocalDateTime alertTime;
}