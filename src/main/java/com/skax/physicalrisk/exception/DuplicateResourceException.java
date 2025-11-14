package com.skax.physicalrisk.exception;

/**
 * 중복된 리소스 예외
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
public class DuplicateResourceException extends BusinessException {

	public DuplicateResourceException(ErrorCode errorCode) {
		super(errorCode);
	}

	public DuplicateResourceException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
