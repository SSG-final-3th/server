package com.exam.answer;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "answer")
@EntityListeners(AuditingEntityListener.class) // Auditing 활성화
public class Answer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long answerId;

	@Column(name = "questionId", nullable = false)
	private Long questionId;  // 질문 ID (외래키로 처리)

	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private String status;  // 답변 상태

	@CreatedDate  // 자동으로 현재 날짜 설정
	@Column(updatable = false, nullable = false)
	private LocalDateTime createDate;
}
