package com.rpm.remotepatientmonitoring.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {

    @NotBlank(message = "Mã bác sĩ bắt buộc phải nhập.")
    @Pattern(regexp = "^(BS|bs)\\d{3,5}$", message = "Mã bác sĩ sai định dạng. Phải bắt đầu bằng 'BS' hoặc 'bs' (Ví dụ: BS001).")
    private String doctorCode;

    @NotBlank(message = "Họ và tên bắt buộc phải nhập.")
    @Pattern(
            regexp = "^(?=.{2,50}$)(?:[\\p{L}]*[aAàÀảẢãÃáÁạẠăĂằẰẳẲẵẴắẮặẶâÂầẦẩẨẫẪấẤậẬeEèÈẻẺẽẼéÉẹẸêÊềỀểỂễỄếẾệỆiIìÌỉỈĩĨíÍịỊoOòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢuUùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰyYỳỲỷỶỹỸýÝỵỴ][\\p{L}]*(?:\\s+|$))+$",
            message = "Họ và tên không hợp lệ. Mỗi từ trong tên đều phải chứa ít nhất một nguyên âm và chỉ dùng chữ cái."
    )
    private String fullName;

    @NotBlank(message = "Số điện thoại bắt buộc phải nhập.")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Số điện thoại không đúng định dạng mạng viễn thông Việt Nam.")
    private String phone;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Địa chỉ email không đúng định dạng.")
    private String email;

    private String password;

    @NotBlank(message = "Chuyên khoa bắt buộc phải chọn.")
    @Pattern(
            regexp = "^(Tiểu đường|Huyết áp|Cả tiểu đường và huyết áp)$",
            message = "Chuyên khoa không hợp lệ."
    )
    private String specialty;

    @NotNull(message = "Giới hạn tải lượng không được trống.")
    @Min(value = 1, message = "Tối thiểu quản lý 1 bệnh nhân.")
    @Max(value = 500, message = "Tối đa quản lý 500 bệnh nhân.")
    private Integer capacityLimit = 50;
}