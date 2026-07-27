package com.rpm.remotepatientmonitoring.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String answer;
    private List<AiCitation> citations;
    private String summaryTakeaway;
    private String disclaimer;
    private String confidenceLevel;
}
