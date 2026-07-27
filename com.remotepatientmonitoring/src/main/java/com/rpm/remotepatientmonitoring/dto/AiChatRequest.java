package com.rpm.remotepatientmonitoring.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {
    private String userId;
    private Integer patientId;
    private String question;
    private String context;
}
