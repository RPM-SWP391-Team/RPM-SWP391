package com.rpm.remotepatientmonitoring.dto.hospital;

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

    @NotBlank(message = "Tên hiển thị bệnh viện không được để trống.")
    @Size(max = 255, message = "Tên bệnh viện không được vượt quá 255 ký tự.")
    private String fullName;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự.")
    private String address;

    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$|^$", message = "Số điện thoại đường dây nóng không hợp lệ.")
    private String phone;

    // Các trường thông tin cá nhân của Quản trị viên (HospitalAdmin)
    @NotBlank(message = "Họ tên quản trị viên không được để trống.")
    @Size(max = 150, message = "Họ tên quản trị viên không được vượt quá 150 ký tự.")
    private String adminFullName;

    private String adminCode;

    @Size(max = 100, message = "Phòng ban không được vượt quá 100 ký tự.")
    private String adminDepartment;

    @Size(max = 100, message = "Chức vụ không được vượt quá 100 ký tự.")
    private String adminPosition;

    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$|^$", message = "Số điện thoại cá nhân không hợp lệ.")
    private String adminPhone;

    private String adminRoleType;

    // Các trường phục vụ đổi mật khẩu (Tùy chọn)
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;

    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }

    public String getAdminFullName() { return adminFullName; }
    public void setAdminFullName(String adminFullName) { this.adminFullName = adminFullName; }
    public String getAdminCode() { return adminCode; }
    public void setAdminCode(String adminCode) { this.adminCode = adminCode; }
    public String getAdminDepartment() { return adminDepartment; }
    public void setAdminDepartment(String adminDepartment) { this.adminDepartment = adminDepartment; }
    public String getAdminPosition() { return adminPosition; }
    public void setAdminPosition(String adminPosition) { this.adminPosition = adminPosition; }
    public String getAdminPhone() { return adminPhone; }
    public void setAdminPhone(String adminPhone) { this.adminPhone = adminPhone; }
    public String getAdminRoleType() { return adminRoleType; }
    public void setAdminRoleType(String adminRoleType) { this.adminRoleType = adminRoleType; }
}