package com.skax.physicalrisk.exception;

/**
 * 분석 작업 관련 예외
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
public class AnalysisJobException extends BusinessException {

	public AnalysisJobException(ErrorCode errorCode) {
		super(errorCode);
	}

	public AnalysisJobException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public AnalysisJobException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
