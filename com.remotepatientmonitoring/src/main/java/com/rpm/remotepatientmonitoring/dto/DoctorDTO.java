package com.rpm.remotepatientmonitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {

    @NotBlank(message = "Mã bác sĩ bắt buộc phải nhập.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{2,50}$", message = "Mã chỉ chứa chữ, số, dấu gạch ngang/dưới (2-50 ký tự).")
    private String doctorCode;

    @NotBlank(message = "Họ và tên bắt buộc phải nhập.")
    @Pattern(
            regexp = "^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠƠƯỨỨỬỮỰẤẤẨẪẬẮẮẲẴẬéèẻẽéêềếểễệíìỉĩịóòỏõọôồốổỗộơờớởỡợúùủũụưừứửữựýỳỷỹỵ\\s]{2,50}$",
            message = "Họ và tên không hợp lệ. Chỉ được phép nhập chữ cái và khoảng trắng (độ dài từ 2-50 ký tự, không chứa số hoặc ký tự đặc biệt)."
    )
    private String fullName;

    @NotBlank(message = "Số điện thoại bắt buộc phải nhập.")
    @Pattern(regexp = "^[0-9]{9,15}$", message = "Số điện thoại không đúng định dạng (9-15 chữ số).")
    private String phone;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Địa chỉ email không đúng cú pháp y tế.")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(min = 6, message = "Mật khẩu ban đầu phải có từ 6 ký tự trở lên.")
    private String password;

    // SỬA: Thêm luật chặn cứng 3 nhóm chuyên khoa cố định
    @NotBlank(message = "Chuyên khoa bắt buộc phải chọn.")
    @Pattern(
            regexp = "^(Tiểu đường|Huyết áp|Cả tiểu đường và huyết áp)$",
            message = "Chuyên khoa không hợp lệ. Chỉ chấp nhận: 'Tiểu đường', 'Huyết áp' hoặc 'Cả tiểu đường và huyết áp'."
    )
    private String specialty;

    @NotNull(message = "Giới hạn tải lượng không được trống.")
    @Min(value = 1, message = "Tối thiểu quản lý 1 bệnh nhân.")
    @Max(value = 500, message = "Tối đa quản lý 500 bệnh nhân.")
    private Integer capacityLimit = 50;
}