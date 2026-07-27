package com.rpm.remotepatientmonitoring.dto;

public class AiSummaryRequest {
    private String patientInfo;
    private String adaStatsText;
    private String bpStatsText;
    private String currentTreatment;

    public AiSummaryRequest() {}

    public String getPatientInfo() { return patientInfo; }
    public void setPatientInfo(String patientInfo) { this.patientInfo = patientInfo; }

    public String getAdaStatsText() { return adaStatsText; }
    public void setAdaStatsText(String adaStatsText) { this.adaStatsText = adaStatsText; }

    public String getBpStatsText() { return bpStatsText; }
    public void setBpStatsText(String bpStatsText) { this.bpStatsText = bpStatsText; }

    public String getCurrentTreatment() { return currentTreatment; }
    public void setCurrentTreatment(String currentTreatment) { this.currentTreatment = currentTreatment; }
}
