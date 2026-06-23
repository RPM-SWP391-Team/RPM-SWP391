package com.rpm.remotepatientmonitoring.dto;

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
    @NotNull(message = "Ngưỡng tối thiểu bình thường không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseNormalMin;

    @NotNull(message = "Ngưỡng tối đa bình thường không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseNormalMax;

    @NotNull(message = "Ngưỡng tối thiểu tiền tiểu đường không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseWarningMin;

    @NotNull(message = "Ngưỡng tối đa tiền tiểu đường không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseWarningMax;

    @NotNull(message = "Ngưỡng tối thiểu trong điều trị không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseTreatingMin;

    @NotNull(message = "Ngưỡng tối đa trong điều trị không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseTreatingMax;

    @NotNull(message = "Ngưỡng nguy hiểm cấp báo không được để trống")
    @DecimalMin(value = "0.1", message = "Chỉ số đường huyết phải là số dương lớn hơn 0")
    private BigDecimal glucoseDangerThreshold;

    // --- Systolic Blood Pressure ---
    @NotNull(message = "Huyết áp tâm thu bình thường không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicNormalMax;

    @NotNull(message = "Ngưỡng tối thiểu tiền tăng huyết áp không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicPrehypertensionMin;

    @NotNull(message = "Ngưỡng tối đa tiền tăng huyết áp không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicPrehypertensionMax;

    @NotNull(message = "Ngưỡng tối thiểu tăng huyết áp không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicHypertensionMin;

    @NotNull(message = "Ngưỡng tối đa tăng huyết áp không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicHypertensionMax;

    @NotNull(message = "Ngưỡng nguy hiểm tâm thu không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicDangerThreshold;

    @NotNull(message = "Ngưỡng cấp cứu tâm thu không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm thu phải là số nguyên dương")
    private Integer systolicEmergencyThreshold;

    // --- Diastolic Blood Pressure ---
    @NotNull(message = "Huyết áp tâm trương bình thường không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicNormalMax;

    @NotNull(message = "Ngưỡng tối thiểu tăng huyết áp tâm trương không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicHypertensionMin;

    @NotNull(message = "Ngưỡng tối đa tăng huyết áp tâm trương không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicHypertensionMax;

    @NotNull(message = "Ngưỡng nguy hiểm tâm trương không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicDangerThreshold;

    @NotNull(message = "Ngưỡng cấp cứu tâm trương không được để trống")
    @Min(value = 1, message = "Chỉ số huyết áp tâm trương phải là số nguyên dương")
    private Integer diastolicEmergencyThreshold;
}