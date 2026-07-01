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
}