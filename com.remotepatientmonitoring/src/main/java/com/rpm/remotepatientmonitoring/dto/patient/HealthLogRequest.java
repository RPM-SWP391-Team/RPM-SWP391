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
}
