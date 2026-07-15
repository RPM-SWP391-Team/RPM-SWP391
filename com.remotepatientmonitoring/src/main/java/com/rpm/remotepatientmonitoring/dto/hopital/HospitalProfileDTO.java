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

    // Trường định danh nội bộ (Read-only)
    private String hospitalCode;

    // Các trường thông tin cơ bản
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không đúng định dạng.")
    private String email;

    @NotBlank(message = "Tên bệnh viện không được để trống.")
    @Size(min = 5, max = 255, message = "Tên bệnh viện phải từ 5 đến 255 ký tự.")
    private String fullName;

    @NotBlank(message = "Địa chỉ không được để trống.")
    @Size(min = 5, max = 500, message = "Địa chỉ phải từ 5 đến 500 ký tự.")
    private String address;

    @NotBlank(message = "Số điện thoại bắt buộc phải nhập.")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Số điện thoại không đúng định dạng mạng viễn thông Việt Nam (phải gồm 10 chữ số và bắt đầu bằng 03, 05, 07, 08, 09).")
    private String phone;

    // Các trường phục vụ đổi mật khẩu (Tùy chọn)
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}