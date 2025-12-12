package com.skax.physicalrisk.client.gmail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gmail API 발송 응답 DTO
 *
 * Gmail API messages.send 응답
 *
 * 파일 버전: v01
 * 최종 수정일: 2025-12-12
 *
 * @author SKAX Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailSendResponseDto {

    /**
     * 발송된 메시지 ID
     */
    private String id;

    /**
     * 메시지가 속한 스레드 ID
     */
    private String threadId;

    /**
     * 라벨 ID 목록
     */
    private String[] labelIds;
}
