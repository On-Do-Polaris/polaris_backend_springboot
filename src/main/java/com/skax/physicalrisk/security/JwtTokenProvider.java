package com.skax.physicalrisk.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증 Provider
 *
 * 최종 수정일: 2025-11-13
 * 파일 버전: v01
 *
 * @author SKAX Team
 */
@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	/**
	 * Access Token 생성
	 *
	 * @param userId 사용자 ID
	 * @return Access Token
	 */
	public String createAccessToken(UUID userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

		return Jwts.builder()
			.subject(userId.toString())
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigningKey())
			.compact();
	}

	/**
	 * Refresh Token 생성
	 *
	 * @param userId 사용자 ID
	 * @return Refresh Token
	 */
	public String createRefreshToken(UUID userId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

		return Jwts.builder()
			.subject(userId.toString())
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigningKey())
			.compact();
	}

	/**
	 * 토큰에서 사용자 ID 추출
	 *
	 * @param token JWT 토큰
	 * @return 사용자 ID
	 */
	public UUID getUserIdFromToken(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();

		return UUID.fromString(claims.getSubject());
	}

	/**
	 * 토큰 유효성 검증
	 *
	 * @param token JWT 토큰
	 * @return 유효 여부
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (SecurityException ex) {
			log.error("Invalid JWT signature: {}", ex.getMessage());
		} catch (MalformedJwtException ex) {
			log.error("Invalid JWT token: {}", ex.getMessage());
		} catch (ExpiredJwtException ex) {
			log.error("Expired JWT token: {}", ex.getMessage());
		} catch (UnsupportedJwtException ex) {
			log.error("Unsupported JWT token: {}", ex.getMessage());
		} catch (IllegalArgumentException ex) {
			log.error("JWT claims string is empty: {}", ex.getMessage());
		}
		return false;
	}

	/**
	 * Signing Key 생성
	 *
	 * @return SecretKey
	 */
	private SecretKey getSigningKey() {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
