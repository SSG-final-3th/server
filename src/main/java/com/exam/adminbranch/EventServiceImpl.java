package com.exam.adminbranch;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;

	@Autowired
	public EventServiceImpl(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public List<Event> getAllEventsByBranch(String branch) {
		if (branch.equals("default")) {
			return eventRepository.findAll();
		}
		return eventRepository.findByBranchName(branch);
	}

	@Override
	public void createEvent(EventDTO dto) {
		Event event = Event.builder()
			.category(dto.getCategory())
			.branchName(dto.getBranchName())
			.eventTitle(dto.getEventTitle())
			.build();

		eventRepository.save(event);
	}

	@Override
	@Transactional
	public void updateEvent(String eventId, EventDTO dto) {
		Event existingEvent = eventRepository.findById(eventId)
			.orElseThrow(() -> new IllegalArgumentException("해당 ID의 이벤트가 존재하지 않습니다."));

		existingEvent.setCategory(dto.getCategory());
		existingEvent.setBranchName(dto.getBranchName());
		existingEvent.setEventTitle(dto.getEventTitle());


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
}