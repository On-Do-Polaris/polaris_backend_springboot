package com.skax.physicalrisk.exception;

import com.skax.physicalrisk.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * BusinessException 처리
	 *
	 * @param ex BusinessException
	 * @return ErrorResponse
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		log.error("Business exception occurred: {} - {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(ex.getErrorCode().getCode())
			.message(ex.getMessage())
			.timestamp(LocalDateTime.now())
			.build();

		return ResponseEntity
			.status(getHttpStatus(ex.getErrorCode()))
			.body(errorResponse);
	}

	/**
	 * ResourceNotFoundException 처리
	 *
	 * @param ex ResourceNotFoundException
	 * @return ErrorResponse
	 */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
		log.error("Resource not found: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(ex.getErrorCode().getCode())
			.message(ex.getMessage())
			.timestamp(LocalDateTime.now())
			.build();

		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(errorResponse);
	}

	/**
	 * UnauthorizedException 처리
	 *
	 * @param ex UnauthorizedException
	 * @return ErrorResponse
	 */
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
		log.error("Unauthorized: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(ex.getErrorCode().getCode())
			.message(ex.getMessage())
			.timestamp(LocalDateTime.now())
			.build();

		return ResponseEntity
			.status(HttpStatus.UNAUTHORIZED)
			.body(errorResponse);
	}

	/**
	 * DuplicateResourceException 처리
	 *
	 * @param ex DuplicateResourceException
	 * @return ErrorResponse
	 */
	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
		log.error("Duplicate resource: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(ex.getErrorCode().getCode())
			.message(ex.getMessage())
			.timestamp(LocalDateTime.now())
			.build();

		return ResponseEntity
			.status(HttpStatus.CONFLICT)
			.body(errorResponse);
	}

	/**
	 * Validation 예외 처리
	 *
	 * @param ex MethodArgumentNotValidException
	 * @return ErrorResponse
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
			.map(FieldError::getDefaultMessage)
			.collect(Collectors.joining(", "));

		log.error("Validation error: {}", message);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(ErrorCode.INVALID_REQUEST.getCode())
			.message(message)
			.timestamp(LocalDateTime.now())
			.build();

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(errorResponse);
	}

	/**
	 * 기타 모든 예외 처리
	 *
	 * @param ex Exception
	 * @return ErrorResponse
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.error("Unexpected error occurred", ex);

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
			.message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
			.timestamp(LocalDateTime.now())
			.build();

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(errorResponse);
	}

	/**
	 * ErrorCode에 따른 HTTP 상태 코드 반환
	 *
	 * @param errorCode 에러 코드
	 * @return HTTP 상태 코드
	 */
	private HttpStatus getHttpStatus(ErrorCode errorCode) {
		return switch (errorCode) {
			case UNAUTHORIZED, INVALID_CREDENTIALS, INVALID_TOKEN, TOKEN_EXPIRED -> HttpStatus.UNAUTHORIZED;
			case ACCESS_DENIED -> HttpStatus.FORBIDDEN;
			case RESOURCE_NOT_FOUND, USER_NOT_FOUND, SITE_NOT_FOUND, REPORT_NOT_FOUND, ANALYSIS_JOB_NOT_FOUND ->
				HttpStatus.NOT_FOUND;
			case EMAIL_ALREADY_EXISTS, DUPLICATE_RESOURCE, ANALYSIS_ALREADY_RUNNING -> HttpStatus.CONFLICT;
			case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
			default -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
	}
}
