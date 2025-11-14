package com.skax.physicalrisk.exception;

/**
 * 리소스를 찾을 수 없음 예외
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
public class ResourceNotFoundException extends BusinessException {

	public ResourceNotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ResourceNotFoundException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
