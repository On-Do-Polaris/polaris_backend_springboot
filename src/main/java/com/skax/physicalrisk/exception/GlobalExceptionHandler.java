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
			.result("error")
			.message(ex.getMessage())
			.errorCode(ex.getErrorCode().getCode())
			.code(ex.getErrorCode().getCode())
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
			.result("error")
			.message(ex.getMessage())
			.errorCode(ex.getErrorCode().getCode())
			.code(ex.getErrorCode().getCode())
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
			.result("error")
			.message(ex.getMessage())
			.errorCode(ex.getErrorCode().getCode())
			.code(ex.getErrorCode().getCode())
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
			.result("error")
			.message(ex.getMessage())
			.errorCode(ex.getErrorCode().getCode())
			.code(ex.getErrorCode().getCode())
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
			.result("error")
			.message(message)
			.errorCode(ErrorCode.INVALID_REQUEST.getCode())
			.code(ErrorCode.INVALID_REQUEST.getCode())
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
			.result("error")
			.message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
			.errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
			.code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
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
			// 401 Unauthorized: 인증 실패
			case UNAUTHORIZED, INVALID_CREDENTIALS, INVALID_TOKEN, TOKEN_EXPIRED,
				 ACCOUNT_NOT_FOUND, EMAIL_NOT_FOUND, PASSWORD_MISMATCH -> HttpStatus.UNAUTHORIZED;

			// 403 Forbidden: 권한 없음
			case ACCESS_DENIED -> HttpStatus.FORBIDDEN;

			// 404 Not Found: 리소스를 찾을 수 없음
			case RESOURCE_NOT_FOUND, USER_NOT_FOUND, SITE_NOT_FOUND, SITE_NAME_NOT_FOUND,
				 REPORT_NOT_FOUND, ANALYSIS_JOB_NOT_FOUND, ANALYSIS_RESULT_NOT_FOUND,
				 VERIFICATION_CODE_NOT_FOUND -> HttpStatus.NOT_FOUND;

			// 409 Conflict: 리소스 중복
			case DUPLICATE_EMAIL, EMAIL_ALREADY_EXISTS, DUPLICATE_RESOURCE,
				 ANALYSIS_ALREADY_RUNNING, EMAIL_ALREADY_VERIFIED -> HttpStatus.CONFLICT;

			// 422 Unprocessable Entity: 검증 실패
			case VERIFICATION_CODE_REQUIRED, VERIFICATION_CODE_MISMATCH, VERIFICATION_CODE_EXPIRED,
				 EMAIL_NOT_VERIFIED, INVALID_EMAIL_FORMAT, INVALID_PASSWORD_FORMAT,
				 INVALID_SITE_DATA -> HttpStatus.UNPROCESSABLE_ENTITY;

			// 400 Bad Request: 잘못된 요청
			case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;

			// 503 Service Unavailable: 외부 서비스 오류
			case EMAIL_SEND_FAILED, EMAIL_SERVICE_UNAVAILABLE,
				 FASTAPI_CONNECTION_ERROR, FASTAPI_TIMEOUT, FASTAPI_INVALID_RESPONSE,
				 SIMULATION_FAILED -> HttpStatus.SERVICE_UNAVAILABLE;

			// 500 Internal Server Error: 서버 내부 오류
			case INTERNAL_SERVER_ERROR, ANALYSIS_FAILED, REPORT_GENERATION_FAILED,
				 FILE_UPLOAD_FAILED, FILE_DOWNLOAD_FAILED, REPORT_EXPIRED -> HttpStatus.INTERNAL_SERVER_ERROR;

			default -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
	}
}
