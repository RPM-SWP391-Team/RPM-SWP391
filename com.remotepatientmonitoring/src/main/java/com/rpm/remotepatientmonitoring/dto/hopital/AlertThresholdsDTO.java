package com.rpm.remotepatientmonitoring.dto.hopital;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertThresholdsDTO {

    private Integer id;

    // --- Glucose thresholds ---
    @NotNull(message = "Ngưỡng hạ đường huyết không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseHypoThreshold;

    @NotNull(message = "Ngưỡng bình thường tối đa không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseNormalMax;

    @NotNull(message = "Ngưỡng cao tối đa không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseHighMax;

    // --- Systolic Blood Pressure ---
    @NotNull(message = "Huyết áp tâm thu bình thường không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicNormalMax;

    @NotNull(message = "Ngưỡng cảnh báo tâm thu tối thiểu không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicWarningMin;

    @NotNull(message = "Ngưỡng cảnh báo tâm thu tối đa không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicWarningMax;

    @NotNull(message = "Ngưỡng nguy hiểm tâm thu tối thiểu không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicDangerMin;

    @NotNull(message = "Ngưỡng nguy hiểm tâm thu tối đa không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicDangerMax;

    @NotNull(message = "Ngưỡng cấp cứu tâm thu không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicEmergencyThreshold;

    // --- Diastolic Blood Pressure ---
    @NotNull(message = "Huyết áp tâm trương bình thường không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicNormalMax;

    @NotNull(message = "Ngưỡng cảnh báo tâm trương tối thiểu không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicWarningMin;

    @NotNull(message = "Ngưỡng cảnh báo tâm trương tối đa không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicWarningMax;

    @NotNull(message = "Ngưỡng nguy hiểm tâm trương tối thiểu không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicDangerMin;

    @NotNull(message = "Ngưỡng nguy hiểm tâm trương tối đa không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicDangerMax;

    @NotNull(message = "Ngưỡng cấp cứu tâm trương không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicEmergencyThreshold;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getGlucoseHypoThreshold() { return glucoseHypoThreshold; }
    public void setGlucoseHypoThreshold(BigDecimal glucoseHypoThreshold) { this.glucoseHypoThreshold = glucoseHypoThreshold; }
    public BigDecimal getGlucoseNormalMax() { return glucoseNormalMax; }
    public void setGlucoseNormalMax(BigDecimal glucoseNormalMax) { this.glucoseNormalMax = glucoseNormalMax; }
    public BigDecimal getGlucoseHighMax() { return glucoseHighMax; }
    public void setGlucoseHighMax(BigDecimal glucoseHighMax) { this.glucoseHighMax = glucoseHighMax; }
    public Integer getSystolicNormalMax() { return systolicNormalMax; }
    public void setSystolicNormalMax(Integer systolicNormalMax) { this.systolicNormalMax = systolicNormalMax; }
    public Integer getSystolicWarningMin() { return systolicWarningMin; }
    public void setSystolicWarningMin(Integer systolicWarningMin) { this.systolicWarningMin = systolicWarningMin; }
    public Integer getSystolicWarningMax() { return systolicWarningMax; }
    public void setSystolicWarningMax(Integer systolicWarningMax) { this.systolicWarningMax = systolicWarningMax; }
    public Integer getSystolicDangerMin() { return systolicDangerMin; }
    public void setSystolicDangerMin(Integer systolicDangerMin) { this.systolicDangerMin = systolicDangerMin; }
    public Integer getSystolicDangerMax() { return systolicDangerMax; }
    public void setSystolicDangerMax(Integer systolicDangerMax) { this.systolicDangerMax = systolicDangerMax; }
    public Integer getSystolicEmergencyThreshold() { return systolicEmergencyThreshold; }
    public void setSystolicEmergencyThreshold(Integer systolicEmergencyThreshold) { this.systolicEmergencyThreshold = systolicEmergencyThreshold; }
    public Integer getDiastolicNormalMax() { return diastolicNormalMax; }
    public void setDiastolicNormalMax(Integer diastolicNormalMax) { this.diastolicNormalMax = diastolicNormalMax; }
    public Integer getDiastolicWarningMin() { return diastolicWarningMin; }
    public void setDiastolicWarningMin(Integer diastolicWarningMin) { this.diastolicWarningMin = diastolicWarningMin; }
    public Integer getDiastolicWarningMax() { return diastolicWarningMax; }
    public void setDiastolicWarningMax(Integer diastolicWarningMax) { this.diastolicWarningMax = diastolicWarningMax; }
    public Integer getDiastolicDangerMin() { return diastolicDangerMin; }
    public void setDiastolicDangerMin(Integer diastolicDangerMin) { this.diastolicDangerMin = diastolicDangerMin; }
    public Integer getDiastolicDangerMax() { return diastolicDangerMax; }
    public void setDiastolicDangerMax(Integer diastolicDangerMax) { this.diastolicDangerMax = diastolicDangerMax; }
    public Integer getDiastolicEmergencyThreshold() { return diastolicEmergencyThreshold; }
    public void setDiastolicEmergencyThreshold(Integer diastolicEmergencyThreshold) { this.diastolicEmergencyThreshold = diastolicEmergencyThreshold; }
}