package com.rpm.remotepatientmonitoring.dto.hospital;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyGuideDTO {
    private Integer id;

    @NotBlank(message = "Cấp độ cảnh báo không được để trống.")
    @Pattern(regexp = "^(GREEN|YELLOW|ORANGE|RED)$", message = "Cấp độ cảnh báo không hợp lệ.")
    private String alertLevel;

    @NotBlank(message = "Loại chỉ số không được để trống.")
    @Pattern(regexp = "^(GLUCOSE|BLOOD_PRESSURE|BOTH)$", message = "Loại chỉ số không hợp lệ.")
    private String metricType;

    @NotBlank(message = "Tiêu đề chỉ dẫn không được để trống.")
    @Size(min = 5, max = 150, message = "Tiêu đề phải từ 5 đến 150 ký tự.")
    private String title;

    @NotBlank(message = "Nội dung chỉ dẫn không được để trống.")
    @Size(min = 20, max = 2000, message = "Nội dung chỉ dẫn phải từ 20 đến 2000 ký tự.")
    private String instructionContent;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getInstructionContent() { return instructionContent; }
    public void setInstructionContent(String instructionContent) { this.instructionContent = instructionContent; }
}