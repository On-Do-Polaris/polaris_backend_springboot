package com.skax.physicalrisk.client.gmail;

import com.skax.physicalrisk.client.gmail.dto.GmailMessageDto;
import com.skax.physicalrisk.client.gmail.dto.GmailSendResponseDto;
import com.skax.physicalrisk.exception.BusinessException;
import com.skax.physicalrisk.exception.ErrorCode;
import com.skax.physicalrisk.service.oauth.GoogleOAuthService;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

/**
 * Gmail API 클라이언트
 *
 * Gmail API를 사용한 이메일 발송
 * - RFC 2822 형식의 MIME 메시지 생성
 * - Base64URL 인코딩
 * - Gmail API messages.send 호출
 *
 * 파일 버전: v01
 * 최종 수정일: 2025-12-12
 *
 * @author SKAX Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GmailClient {

    private final GoogleOAuthService oauthService;
    private final WebClient webClient;

    @Value("${google.gmail.sender-email}")
    private String senderEmail;

    /**
     * WebClient 빈 생성
     */
    public GmailClient(GoogleOAuthService oauthService) {
        this.oauthService = oauthService;
        this.webClient = WebClient.builder()
            .baseUrl("https://www.googleapis.com/gmail/v1")
            .build();
    }

    /**
     * Gmail API를 통한 이메일 발송
     *
     * @param to      수신자 이메일
     * @param subject 제목
     * @param body    본문
     */
    public void sendEmail(String to, String subject, String body) {
        log.info("Gmail 발송 시작: to={}, subject={}", to, subject);

        try {
            // 1. 유효한 Access Token 획득 (만료 시 자동 갱신)
            String accessToken = oauthService.getValidAccessToken();

            // 2. RFC 2822 형식의 MIME 메시지 생성 및 Base64URL 인코딩
            String rawMessage = createRawMessage(to, subject, body);

            // 3. Gmail API 호출
            GmailMessageDto messageDto = new GmailMessageDto(rawMessage);

            GmailSendResponseDto response = webClient.post()
                .uri("/users/me/messages/send")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .bodyValue(messageDto)
                .retrieve()
                .bodyToMono(GmailSendResponseDto.class)
                .block();

            if (response != null) {
                log.info("Gmail 발송 성공: to={}, subject={}, messageId={}", to, subject, response.getId());
            } else {
                log.warn("Gmail 발송 응답이 null: to={}, subject={}", to, subject);
            }

        } catch (WebClientResponseException e) {
            log.error("Gmail API 호출 실패: status={}, to={}, subject={}, body={}",
                e.getStatusCode(), to, subject, e.getResponseBodyAsString());

            // 401: Access Token 문제
            if (e.getStatusCode().value() == 401) {
                throw new BusinessException(ErrorCode.OAUTH_TOKEN_REFRESH_FAILED,
                    "Gmail API 인증 실패. OAuth 토큰을 확인하세요");
            }

            // 429: 발송 한도 초과
            if (e.getStatusCode().value() == 429) {
                throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED,
                    "Gmail API 발송 한도 초과. 잠시 후 다시 시도하세요");
            }

            // 기타 에러
            throw new BusinessException(ErrorCode.GMAIL_API_ERROR,
                "Gmail 발송 실패: " + e.getMessage());

        } catch (BusinessException e) {
            // OAuth 관련 예외는 그대로 전파
            throw e;

        } catch (Exception e) {
            log.error("Gmail 발송 중 예외 발생: to={}, subject={}", to, subject, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED,
                "이메일 발송 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * RFC 2822 형식의 MIME 메시지 생성 및 Base64URL 인코딩
     *
     * @param to      수신자 이메일
     * @param subject 제목
     * @param body    본문
     * @return Base64URL 인코딩된 메시지
     */
    private String createRawMessage(String to, String subject, String body) {
        try {
            // JavaMail을 사용하여 MIME 메시지 생성
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress(senderEmail));
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
            email.setSubject(subject, "UTF-8");
            email.setText(body, "UTF-8", "plain");

            // MIME 메시지를 바이트 배열로 변환
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            byte[] bytes = buffer.toByteArray();

            // Base64URL 인코딩 (패딩 제거, +를 -, /를 _로 치환)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        } catch (MessagingException e) {
            log.error("MIME 메시지 생성 실패: to={}, subject={}", to, subject, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED,
                "이메일 메시지 생성 실패: " + e.getMessage());

        } catch (IOException e) {
            log.error("MIME 메시지 인코딩 실패: to={}, subject={}", to, subject, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED,
                "이메일 메시지 인코딩 실패: " + e.getMessage());
        }
    }
}
