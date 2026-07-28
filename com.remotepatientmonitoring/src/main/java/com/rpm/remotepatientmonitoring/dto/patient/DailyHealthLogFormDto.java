package com.rpm.remotepatientmonitoring.dto.patient;

import com.rpm.remotepatientmonitoring.validator.ValidHealthLog;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;

@Data
@ValidHealthLog
public class DailyHealthLogFormDto {
    private Integer id;
    private String logType;
    private String inputMethod = "MANUAL";

    @Min(value = 50, message = "Chỉ số tâm thu phải từ 50 đến 300 mmHg.")
    @Max(value = 300, message = "Chỉ số tâm thu phải từ 50 đến 300 mmHg.")
    private Integer systolicBp;

    @Min(value = 30, message = "Chỉ số tâm trương phải từ 30 đến 200 mmHg.")
    @Max(value = 200, message = "Chỉ số tâm trương phải từ 30 đến 200 mmHg.")
    private Integer diastolicBp;

    @Min(value = 20, message = "Nhịp tim phải từ 20 đến 300 lần/phút.")
    @Max(value = 300, message = "Nhịp tim phải từ 20 đến 300 lần/phút.")
    private Integer heartRate;

    @DecimalMin(value = "1.0", message = "Chỉ số Đường huyết phải từ 1.0 đến 33.3 mmol/L.")
    @DecimalMax(value = "33.3", message = "Chỉ số Đường huyết phải từ 1.0 đến 33.3 mmol/L.")
    private BigDecimal glucoseLevel;

    private String patientNotes;

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
    public String getPatientNotes() { return patientNotes; }
    public void setPatientNotes(String patientNotes) { this.patientNotes = patientNotes; }
}
