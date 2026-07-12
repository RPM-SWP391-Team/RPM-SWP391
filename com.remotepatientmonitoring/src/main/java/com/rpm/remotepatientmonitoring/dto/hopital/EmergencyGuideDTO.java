package com.rpm.remotepatientmonitoring.dto.hopital;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyGuideDTO {

    @NotBlank(message = "Mức cảnh báo bắt buộc phải chọn.")
    @Pattern(regexp = "^(GREEN|YELLOW|ORANGE|RED)$", message = "Mức cảnh báo phải là GREEN, YELLOW, ORANGE hoặc RED.")
    @Size(max = 10, message = "Mức cảnh báo không được quá 10 ký tự.")
    private String alertLevel;

    @NotBlank(message = "Chỉ số áp dụng bắt buộc phải chọn.")
    @Pattern(regexp = "^(GLUCOSE|BLOOD_PRESSURE|BOTH)$", message = "Chỉ số áp dụng phải là GLUCOSE, BLOOD_PRESSURE hoặc BOTH.")
    @Size(max = 30, message = "Tên chỉ số không được quá 30 ký tự.")
    private String metricType;

    @NotBlank(message = "Tiêu đề hướng dẫn không được để trống.")
    @Size(min = 5, max = 150, message = "Tiêu đề hướng dẫn phải từ 5 đến 150 ký tự.")
    private String title;

    @NotBlank(message = "Nội dung chỉ dẫn không được để trống.")
    @Size(min = 20, message = "Nội dung chỉ dẫn xử lý khẩn cấp phải chi tiết (Tối thiểu 20 ký tự).")
    private String instructionContent;
}