package com.exam.adminbranch;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class EventDTO {

	@Id
	Long eventId;
	String category;
	String eventTitle;
	LocalDateTime startDate;
	LocalDateTime endDate;
	String image;
	String description;

}
