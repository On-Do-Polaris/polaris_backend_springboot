package com.skax.physicalrisk.exception;

/**
 * FastAPI 호출 실패 예외
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
public class FastApiException extends BusinessException {

	public FastApiException(ErrorCode errorCode) {
		super(errorCode);
	}

	public FastApiException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public FastApiException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
