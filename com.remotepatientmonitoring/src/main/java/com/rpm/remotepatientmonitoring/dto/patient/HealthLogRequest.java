package com.rpm.remotepatientmonitoring.dto.patient;

import lombok.Data;
import java.math.BigDecimal;
@Data
public class HealthLogRequest {
    private Integer patientId;
    private String logType;
    private String inputMethod;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer heartRate;
    private BigDecimal glucoseLevel;
    private String imageUrl;
    private String patientNotes;

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }
    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }
    public String getInputMethod() { return inputMethod; }
    public void setInputMethod(String inputMethod) { this.inputMethod = inputMethod; }
    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }
    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    public BigDecimal getGlucoseLevel() { return glucoseLevel; }
    public void setGlucoseLevel(BigDecimal glucoseLevel) { this.glucoseLevel = glucoseLevel; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPatientNotes() { return patientNotes; }
    public void setPatientNotes(String patientNotes) { this.patientNotes = patientNotes; }
}
