package com.rpm.remotepatientmonitoring.dto.hopital;

import jakarta.validation.constraints.Email;
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
public class HospitalProfileDTO {

    // ID Tài khoản Hệ thống (Read-only)
    private Integer accountId;

    // Trường định danh nội bộ
    private String hospitalCode;

    // Các trường thông tin cơ bản
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không đúng định dạng.")
    private String email;

    private String fullName;
    private String address;
    private String phone;

    // Các trường phục vụ đổi mật khẩu (Tùy chọn)
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}