package com.rpm.remotepatientmonitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryResponse {
    private String clinicalSummary;
    private String alertColor; // "RED", "ORANGE", "YELLOW", "GREEN"
    private List<String> activeAlerts;
    private PatientAdaStats adaStats;
    private PatientBpStats bpStats;
    private List<String> suggestedActions;

    public AiSummaryResponse(String clinicalSummary) {
        this.clinicalSummary = clinicalSummary;
    }
}
