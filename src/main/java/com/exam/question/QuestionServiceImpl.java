package com.exam.question;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

	private final QuestionRepository questionRepository;

	public QuestionServiceImpl(QuestionRepository questionRepository) {
		this.questionRepository = questionRepository;
	}

	/**
	 * 질문 추가
	 */
	@Override
	@Transactional
	public void addQuestion(QuestionDTO questionDTO, String userId) {
		Question question = Question.builder()
			.userId(userId)
			.title(questionDTO.getTitle())
			.content(questionDTO.getContent())
			.status(Question.QuestionStatus.PENDING)
			.build();

		questionRepository.save(question);
	}

	/**
	 * 질문 수정
	 */
	@Override
	@Transactional
	public void updateQuestion(String userId, Long questionId, QuestionDTO questionDTO) {
		// 해당 ID의 질문을 찾음
		Question question = questionRepository.findById(questionId)
			.orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));

		// 작성자 확인 (본인만 수정 가능)
		if (!question.getUserId().equals(userId)) {
			throw new RuntimeException("본인의 질문만 수정할 수 있습니다.");
		}

		// 제목 및 내용 수정
		question.setTitle(questionDTO.getTitle());
		question.setContent(questionDTO.getContent());

		questionRepository.save(question);
	}

	/**
	 * 특정 사용자의 질문 목록 조회
	 */
	@Override
	public List<QuestionDTO> getQuestionsByUser(String userId) {
		List<Question> questions = questionRepository.findByUserId(userId);

		// 엔티티 리스트를 DTO 리스트로 변환
		return questions.stream()
			.map(q -> new QuestionDTO(q.getQuestionid(), q.getUserId(), q.getTitle(), q.getContent(), q.getCreateDate(), q.getStatus().name()))
			.collect(Collectors.toList());
	}
}
