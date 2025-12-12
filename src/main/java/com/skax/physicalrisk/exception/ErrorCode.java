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

	// 인증/인가 에러 (상세 구분)
	UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다"),
	ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "계정이 존재하지 않습니다"),
	EMAIL_NOT_FOUND("EMAIL_NOT_FOUND", "존재하지 않는 이메일입니다"),
	PASSWORD_MISMATCH("PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다"),
	INVALID_CREDENTIALS("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다"),
	INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다"),
	TOKEN_EXPIRED("TOKEN_EXPIRED", "토큰이 만료되었습니다"),
	ACCESS_DENIED("ACCESS_DENIED", "접근 권한이 없습니다"),

	// 이메일 인증 관련
	EMAIL_ALREADY_VERIFIED("EMAIL_ALREADY_VERIFIED", "이미 인증된 이메일입니다"),
	EMAIL_NOT_VERIFIED("EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다"),

	// 인증번호 관련
	VERIFICATION_CODE_REQUIRED("VERIFICATION_CODE_REQUIRED", "인증번호가 필요합니다"),
	VERIFICATION_CODE_MISMATCH("VERIFICATION_CODE_MISMATCH", "인증번호가 일치하지 않습니다"),
	VERIFICATION_CODE_EXPIRED("VERIFICATION_CODE_EXPIRED", "인증번호가 만료되었습니다"),
	VERIFICATION_CODE_NOT_FOUND("VERIFICATION_CODE_NOT_FOUND", "유효한 인증번호를 찾을 수 없습니다"),

	// 리소스 에러
	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "리소스를 찾을 수 없습니다"),
	USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
	SITE_NOT_FOUND("SITE_NOT_FOUND", "사업장을 찾을 수 없습니다"),
	SITE_NAME_NOT_FOUND("SITE_NAME_NOT_FOUND", "해당 이름의 사업장을 찾을 수 없습니다"),
	REPORT_NOT_FOUND("REPORT_NOT_FOUND", "리포트를 찾을 수 없습니다"),
	ANALYSIS_JOB_NOT_FOUND("ANALYSIS_JOB_NOT_FOUND", "분석 작업을 찾을 수 없습니다"),
	ANALYSIS_RESULT_NOT_FOUND("ANALYSIS_RESULT_NOT_FOUND", "분석 결과를 찾을 수 없습니다"),

	// 중복 에러
	DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 존재하는 이메일입니다"),
	EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다"),
	DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "중복된 리소스입니다"),
	DUPLICATE_SITE_COORDINATES("DUPLICATE_SITE_COORDINATES", "동일한 위경도를 가진 사업장이 이미 존재합니다"),

	// 검증 관련 에러
	INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", "유효하지 않은 이메일 형식입니다"),
	INVALID_PASSWORD_FORMAT("INVALID_PASSWORD_FORMAT", "비밀번호는 8자 이상이어야 합니다"),
	INVALID_SITE_DATA("INVALID_SITE_DATA", "사업장 데이터가 유효하지 않습니다"),

	// 분석 관련 에러
	ANALYSIS_ALREADY_RUNNING("ANALYSIS_ALREADY_RUNNING", "이미 실행 중인 분석 작업이 있습니다"),
	ANALYSIS_FAILED("ANALYSIS_FAILED", "분석 작업이 실패했습니다"),
	SIMULATION_FAILED("SIMULATION_FAILED", "시뮬레이션 실행에 실패했습니다"),
	FASTAPI_CONNECTION_ERROR("FASTAPI_CONNECTION_ERROR", "FastAPI 서버 연결에 실패했습니다"),
	FASTAPI_TIMEOUT("FASTAPI_TIMEOUT", "FastAPI 요청 시간이 초과되었습니다"),
	FASTAPI_INVALID_RESPONSE("FASTAPI_INVALID_RESPONSE", "FastAPI 응답이 유효하지 않습니다"),

	// GCP 이메일 서비스 관련
	EMAIL_SEND_FAILED("EMAIL_SEND_FAILED", "이메일 발송에 실패했습니다"),
	EMAIL_SERVICE_UNAVAILABLE("EMAIL_SERVICE_UNAVAILABLE", "이메일 서비스를 사용할 수 없습니다"),

	// Google OAuth 관련
	OAUTH_CODE_EXCHANGE_FAILED("OAUTH_CODE_EXCHANGE_FAILED", "OAuth 인증 코드 교환에 실패했습니다"),
	OAUTH_TOKEN_REFRESH_FAILED("OAUTH_TOKEN_REFRESH_FAILED", "OAuth 토큰 갱신에 실패했습니다"),
	OAUTH_TOKEN_NOT_FOUND("OAUTH_TOKEN_NOT_FOUND", "저장된 OAuth 토큰을 찾을 수 없습니다"),
	GMAIL_API_ERROR("GMAIL_API_ERROR", "Gmail API 호출에 실패했습니다"),

	// 리포트 관련 에러
	REPORT_GENERATION_FAILED("REPORT_GENERATION_FAILED", "리포트 생성에 실패했습니다"),
	REPORT_EXPIRED("REPORT_EXPIRED", "리포트 다운로드 기간이 만료되었습니다"),

	// 파일 관련 에러
	FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다"),
	FILE_DOWNLOAD_FAILED("FILE_DOWNLOAD_FAILED", "파일 다운로드에 실패했습니다");

	private final String code;
	private final String message;
}
