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
public class EmergencyProtocolDTO {

    private Integer id;

    @NotBlank(message = "Loại tình trạng bắt buộc phải chọn.")
    @Pattern(regexp = "^(HYPERTENSIVE_CRISIS|HYPOGLYCEMIA|HYPERGLYCEMIA)$", message = "Loại tình trạng không hợp lệ.")
    private String conditionType;

    @NotBlank(message = "Tiêu đề không được để trống.")
    @Size(min = 5, max = 150, message = "Tiêu đề phải từ 5 đến 150 ký tự.")
    private String title;

    @NotBlank(message = "Dấu hiệu nhận biết không được để trống.")
    @Size(min = 10, message = "Dấu hiệu nhận biết phải từ 10 ký tự trở lên.")
    private String warningSigns;

    @NotBlank(message = "Nội dung chỉ dẫn không được để trống.")
    @Size(min = 20, message = "Nội dung chỉ dẫn xử lý khẩn cấp phải chi tiết (Tối thiểu 20 ký tự).")
    private String instructionContent;

    private Boolean isActive;
}
