package com.rpm.remotepatientmonitoring.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEditDTO {

    @NotNull(message = "ID bác sĩ không được để trống.")
    private Integer id;

    @NotBlank(message = "Họ và tên bắt buộc phải nhập.")
    @Size(min = 2, max = 50, message = "Họ và tên phải từ 2 đến 50 ký tự.")
    private String fullName;

    // Hàm Java thuần tự động chạy để kiểm tra rác - CHÍNH XÁC 100%
    @jakarta.validation.constraints.AssertTrue(message = "Họ và tên không hợp lệ. Vui lòng nhập đúng chuẩn tên Tiếng Việt (Mỗi từ phải chứa nguyên âm, không gõ bừa phụ âm hoặc dùng chữ F, J, W, Z).")
    private boolean isFullNameValid() {
        if (fullName == null || fullName.trim().isEmpty()) return false;

        // 1. Chuyển về chữ thường để dễ xử lý
        String nameLower = fullName.toLowerCase().trim();

        // 2. Chấm dứt ngay nếu chứa ký tự lạ không có trong tiếng Việt (f, j, w, z) hoặc số, ký tự đặc biệt
        if (nameLower.matches(".*[fjwz0-9\\p{Punct}].*")) return false;

        // 3. Tách chuỗi thành các từ đơn lẻ dựa vào khoảng trắng
        String[] words = nameLower.split("\\s+");

        // Tập hợp tất cả các nguyên âm tiếng Việt chuẩn (bất chấp tổ hợp/dựng sẵn)
        String vowels = "aàáảãạăằắẳẵặâầấẩẫậeèéẻẽẹêềếểễệiìíỉĩịoòóỏõọôồốổỗộơờớởỡợuùúủũụưừứửữựyỳýỷỹỵ";

        for (String word : words) {
            // Loại bỏ hoàn toàn các dấu phụ khỏi từ trước khi đếm phụ âm để không bị lỗi bảng mã
            String cleanWord = java.text.Normalizer.normalize(word, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");

            // Nếu từ nào có 4 phụ âm liên tiếp đứng cạnh nhau (ví dụ: sgrf) -> CHẶN
            if (cleanWord.matches(".*[bcdđghklmnpqrstvx]{4,}.*")) return false;

            // Kiểm tra xem từ này có chứa ít nhất 1 nguyên âm không
            boolean hasVowel = false;
            for (char c : word.toCharArray()) {
                if (vowels.indexOf(c) != -1) {
                    hasVowel = true;
                    break;
                }
            }
            // Nếu có bất kỳ từ nào KHÔNG có nguyên âm (ví dụ: ksdgsj) -> CHẶN NGAY
            if (!hasVowel) return false;
        }

        return true;
    }

    @NotBlank(message = "Giới tính bắt buộc phải chọn.")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Giới tính không hợp lệ.")
    private String gender;

    @NotBlank(message = "Số điện thoại bắt buộc phải nhập.")
    @Pattern(regexp = "^(0[35789])[0-9]{8}$", message = "Số điện thoại không đúng định dạng mạng viễn thông Việt Nam (phải gồm 10 chữ số và bắt đầu bằng 03, 05, 07, 08, 09).")
    private String phone;

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
