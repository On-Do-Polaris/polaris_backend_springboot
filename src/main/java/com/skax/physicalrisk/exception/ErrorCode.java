package com.skax.physicalrisk.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 열거형
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 일반 에러
	INVALID_REQUEST("INVALID_REQUEST", "잘못된 요청입니다"),
	INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다"),

	// 인증/인가 에러
	UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다"),
	INVALID_CREDENTIALS("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다"),
	INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다"),
	TOKEN_EXPIRED("TOKEN_EXPIRED", "토큰이 만료되었습니다"),
	ACCESS_DENIED("ACCESS_DENIED", "접근 권한이 없습니다"),

	// 리소스 에러
	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "리소스를 찾을 수 없습니다"),
	USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
	SITE_NOT_FOUND("SITE_NOT_FOUND", "사업장을 찾을 수 없습니다"),
	REPORT_NOT_FOUND("REPORT_NOT_FOUND", "리포트를 찾을 수 없습니다"),
	ANALYSIS_JOB_NOT_FOUND("ANALYSIS_JOB_NOT_FOUND", "분석 작업을 찾을 수 없습니다"),

	// 중복 에러
	DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 존재하는 이메일입니다"),
	EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다"),
	DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "중복된 리소스입니다"),

	// 분석 관련 에러
	ANALYSIS_ALREADY_RUNNING("ANALYSIS_ALREADY_RUNNING", "이미 실행 중인 분석 작업이 있습니다"),
	ANALYSIS_FAILED("ANALYSIS_FAILED", "분석 작업이 실패했습니다"),
	FASTAPI_CONNECTION_ERROR("FASTAPI_CONNECTION_ERROR", "FastAPI 서버 연결에 실패했습니다"),

	// 리포트 관련 에러
	REPORT_GENERATION_FAILED("REPORT_GENERATION_FAILED", "리포트 생성에 실패했습니다"),
	REPORT_EXPIRED("REPORT_EXPIRED", "리포트 다운로드 기간이 만료되었습니다"),

	// 파일 관련 에러
	FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다"),
	FILE_DOWNLOAD_FAILED("FILE_DOWNLOAD_FAILED", "파일 다운로드에 실패했습니다");

	private final String code;
	private final String message;
}
