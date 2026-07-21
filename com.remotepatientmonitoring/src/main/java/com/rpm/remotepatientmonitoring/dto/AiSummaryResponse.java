package com.rpm.remotepatientmonitoring.dto;

public class AiSummaryResponse {
    private String clinicalSummary;

    public AiSummaryResponse() {}

    public AiSummaryResponse(String clinicalSummary) {
        this.clinicalSummary = clinicalSummary;
    }

    public String getClinicalSummary() {
        return clinicalSummary;
    }

    public void setClinicalSummary(String clinicalSummary) {
        this.clinicalSummary = clinicalSummary;
    }
}
