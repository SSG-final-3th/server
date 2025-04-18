package com.exam.adminbranch;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

	//카테고리로 event 찾기
	List<Event> findByCategory(String category);

	// 유효기간이 주어진 날짜 이전인 이벤트를 삭제
	void deleteByEndDateBefore(LocalDateTime now);
	// 기본 CRUD 메서드는 JpaRepository에서 자동으로 제공된다... 혁신
	// save(Event entity) - 생성 및 수정
	// findById(Integer id) - ID로 조회
	// deleteById(Integer id) - ID로 삭제
	// existsById(Integer id) - ID로 존재 여부 확인

}
