package com.skax.physicalrisk.client.gmail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gmail API 메시지 DTO
 *
 * Gmail API의 messages.send 요청에 사용
 * raw 필드에 Base64URL 인코딩된 이메일 메시지를 담음
 *
 * 파일 버전: v01
 * 최종 수정일: 2025-12-12
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailMessageDto {

    /**
     * Base64URL 인코딩된 RFC 2822 이메일 메시지
     */
    private String raw;
}
