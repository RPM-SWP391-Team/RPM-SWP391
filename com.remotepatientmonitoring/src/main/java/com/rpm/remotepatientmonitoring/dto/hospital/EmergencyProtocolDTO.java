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
public class EmergencyProtocolDTO {
    private Integer id;

    @NotBlank(message = "Nhóm bệnh lý không được để trống.")
    @Pattern(regexp = "^(HYPERTENSIVE_CRISIS|HYPOGLYCEMIA|HYPERGLYCEMIA)$", message = "Nhóm bệnh lý không hợp lệ.")
    private String conditionType;

    @NotBlank(message = "Tiêu đề cẩm nang không được để trống.")
    @Size(min = 5, max = 150, message = "Tiêu đề phải từ 5 đến 150 ký tự.")
    private String title;

    @NotBlank(message = "Dấu hiệu nhận biết không được để trống.")
    @Size(min = 10, max = 500, message = "Dấu hiệu nhận biết phải từ 10 đến 500 ký tự.")
    private String warningSigns;

    @NotBlank(message = "Nội dung chỉ dẫn không được để trống.")
    @Size(min = 20, max = 2000, message = "Nội dung chỉ dẫn phải từ 20 đến 2000 ký tự.")
    private String instructionContent;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getWarningSigns() { return warningSigns; }
    public void setWarningSigns(String warningSigns) { this.warningSigns = warningSigns; }
    public String getInstructionContent() { return instructionContent; }
    public void setInstructionContent(String instructionContent) { this.instructionContent = instructionContent; }
}