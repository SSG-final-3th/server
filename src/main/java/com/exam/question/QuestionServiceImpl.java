package com.exam.question;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exam.answer.Answer;
import com.exam.answer.AnswerRepository;

import jakarta.transaction.Transactional;

@Service
public class QuestionServiceImpl implements QuestionService {

	private final QuestionRepository questionRepository;
	private final AnswerRepository answerRepository;  // AnswerRepository를 주입받습니다.

	public QuestionServiceImpl(QuestionRepository questionRepository, AnswerRepository answerRepository) {
		this.questionRepository = questionRepository;
		this.answerRepository = answerRepository;  // 생성자를 통해 주입
	}

	// 질문 추가
	@Override
	@Transactional
	public void addQuestion(QuestionDTO questionDTO, String userId) {
		Question question = Question.builder()
			.userId(userId)
			.title(questionDTO.getTitle())
			.content(questionDTO.getContent())
			.status(Question.QuestionStatus.WAITING)
			.build();

		questionRepository.save(question);
	}

	// 답변 삭제
	@Override
	public void deleteAnswer(Long answerId, String userId) {
		// 답변이 존재하는지 확인
		Answer answer = answerRepository.findById(answerId)
			.orElseThrow(() -> new RuntimeException("답변을 찾을 수 없습니다."));

		// 사용자가 해당 답변을 삭제할 권한이 있는지 확인
		if (!answer.getUserId().equals(userId)) {
			throw new RuntimeException("본인만 삭제할 수 있습니다.");
		}

		// 답변 삭제
		answerRepository.delete(answer);
	}

	@Override
	@Transactional
	public void deleteQuestion(String userId, Long questionId) {
		// 해당 ID의 질문을 찾음
		Question question = questionRepository.findById(questionId)
			.orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다."));

		// 작성자 확인 (본인만 삭제 가능)
		if (!question.getUserId().equals(userId)) {
			throw new RuntimeException("본인의 질문만 삭제할 수 있습니다.");
		}

		// 질문 삭제
		questionRepository.deleteById(questionId);

	}

	// 질문 수정
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

	@Override
	public List<QuestionDTO> getAllQuestions() {
		// 모든 질문을 가져와서 DTO로 변환 후 반환
		return questionRepository.findAll().stream()
			.map(question -> new QuestionDTO(
				question.getQuestionid(),  // questionid
				question.getUserId(),      // userId
				question.getTitle(),       // title
				question.getContent(),     // content
				question.getCreateDate(),  // createDate
				question.getStatus().name()))  // status (enum을 문자열로 변환)
			.collect(Collectors.toList());
	}

	// 특정 사용자의 질문 목록 조회
	@Override
	public List<QuestionDTO> getQuestionsByUser(String userId) {
		List<Question> questions = questionRepository.findByUserId(userId);

		// 엔티티 리스트를 DTO 리스트로 변환
		return questions.stream()
			.map(q -> new QuestionDTO(q.getQuestionid(), q.getUserId(), q.getTitle(), q.getContent(), q.getCreateDate(),
				q.getStatus().name()))
			.collect(Collectors.toList());
	}

}