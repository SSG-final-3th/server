package com.exam.adminbranch;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exam.category.Likecategories;
import com.exam.category.LikecategoriesRepository;

import jakarta.transaction.Transactional;

@Service
public class EventServiceImpl implements EventService {

	EventRepository eventRepository;
	LikecategoriesRepository likecategoriesRepository;

	@Autowired
	public EventServiceImpl(EventRepository eventRepository, LikecategoriesRepository likecategoriesRepository) {
		this.eventRepository = eventRepository;
		this.likecategoriesRepository = likecategoriesRepository;
	}

	//모든 지점 조회
	@Override
	public List<Event> getAllEvent() {
		// 데이터베이스에서 모든 지점을 조회
		return eventRepository.findAll();
	}

	@Override
	public void createEvent(EventDTO dto) {
		Event event = Event.builder()
			.category(dto.getCategory())
			.eventTitle(dto.getEventTitle())
			.startDate(dto.getStartDate())
			.endDate(dto.getEndDate())
			.image(dto.getImage())
			.description(dto.getDescription())
			.build();

		eventRepository.save(event);
	}

	@Override
	@Transactional
	public void updateEvent(String eventId, EventDTO dto) {
		Event existingEvent = eventRepository.findById(eventId)
			.orElseThrow(() -> new IllegalArgumentException("해당 ID의 이벤트가 존재하지 않습니다."));

		existingEvent.setCategory(dto.getCategory());
		existingEvent.setEventTitle(dto.getEventTitle());
		existingEvent.setStartDate(dto.getStartDate());
		existingEvent.setEndDate(dto.getEndDate());
		existingEvent.setImage(dto.getImage());
		existingEvent.setDescription(dto.getDescription());

		eventRepository.save(existingEvent);
	}

	@Override
	@Transactional
	public void deleteEvent(String eventId) {
		if (!eventRepository.existsById(eventId)) {
			throw new IllegalArgumentException("해당 ID의 이벤트가 존재하지 않습니다.");
		}
		eventRepository.deleteById(eventId);

	}

	@Override
	public List<EventDTO> getFavoriteEvents(String userId) {
		// 선호 카테고리 찾기
		List<Likecategories> likecategoriesList = likecategoriesRepository.findByUserId(userId);

		// 선호 카테고리가 있을 경우 해당 카테고리로 이벤트 찾기
		if (!likecategoriesList.isEmpty()) {
			// 여러 카테고리에서 이벤트를 찾아서 하나의 리스트로 합치기
			return likecategoriesList.stream()
				.map(likecategory -> eventRepository.findByCategory(likecategory.getCategory())) // 카테고리로 이벤트 찾기
				.flatMap(List::stream) // 각 카테고리에서 찾은 리스트를 하나의 스트림으로 평탄화
				.map(this::convertToDTO) // Event를 EventDTO로 변환
				.collect(Collectors.toList()); // 결과 리스트로 수집
		}

		return Collections.emptyList(); // 선호 카테고리가 없을 경우 빈 리스트 반환
	}

	private EventDTO convertToDTO(Event event) {
		return EventDTO.builder()
			.eventId(event.getEventId())
			.category(event.getCategory())
			.eventTitle(event.getEventTitle())
			.startDate(event.getStartDate())
			.endDate(event.getEndDate())
			.image(event.getImage())
			.description(event.getDescription())
			.build();
	}

}