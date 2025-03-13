package com.exam.exception;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class CustomizedResponseEntityExceptionHandler
	extends ResponseEntityExceptionHandler {
	// 사용자 입력 데이터 유효성 예외 처리하는 메서드를 재정의함.

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
		HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.info("logger:유효성 예외처리.: {}", ex.getMessage());
		// 에러메시지 저장할 ErrorDetails 생성
		// ex.getMessage() 하면 출력되는 에러메시지가 너무 많다.
		// ex.getFieldError().getDefaultMessage() 하면 예외 발생된 개별 에러메시지만 출력됨.
		ErrorDetails errorDetails =
			new ErrorDetails(ex.getFieldError().getDefaultMessage(), LocalDate.now(), request.getDescription(false));

		return new ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST);  // 400 에러

	}//end handleMethodArgumentNotValid

	// 사용자 userId 중복 예외처리
	@ExceptionHandler(value = {SQLIntegrityConstraintViolationException.class, ConstraintViolationException.class})
	public ResponseEntity<ErrorDetails> errorPage(Exception e) {
		log.info("logger:사용자 userId 중복 예외처리.: {}", e.getMessage());

		ErrorDetails errorDetails =
			new ErrorDetails("아이디 중복", LocalDate.now(), "사용자 userId 다시 확인하세요");

		return ResponseEntity.status(500).body(errorDetails); // 500 에러
	}
}

// 에러메시지 저장 클래스
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
class ErrorDetails {

	String message;
	LocalDate timestamp;
	String detail;

}




