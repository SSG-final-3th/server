package com.exam.offline;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Table(name = "offlineprice")
public class OfflinePrice {

	@EmbeddedId
	private OfflinePriceId id; // 복합 기본 키

	private int price; // 가격

	String category;
}
