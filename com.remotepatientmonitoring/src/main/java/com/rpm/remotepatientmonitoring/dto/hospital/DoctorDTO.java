package com.rpm.remotepatientmonitoring.dto.hospital;

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
    @Size(min = 2, max = 50, message = "Họ và tên phải từ 2 đến 50 ký tự.")
    private String fullName;

    // Hàm Java thuần tự động chạy để kiểm tra rác - CHÍNH XÁC 100%
    @jakarta.validation.constraints.AssertTrue(message = "Họ và tên không hợp lệ. Vui lòng nhập đúng chuẩn tên Tiếng Việt (Mỗi từ phải chứa nguyên âm, không gõ bừa phụ âm hoặc dùng chữ F, J, W, Z).")
    public boolean isFullNameValid() {
        if (fullName == null || fullName.trim().isEmpty()) return false;

        String nameLower = fullName.toLowerCase().trim();

        // CHỈ CHẶN: số, ký tự đặc biệt, và các chữ j, w, z (Bỏ chữ f vì họ Phan, Phạm, Phong có chữ ph)
        if (nameLower.matches(".*[jwz0-9\\p{Punct}].*")) return false;

        String[] words = nameLower.split("\\s+");
        String vowels = "aàáảãạăằắẳẵặâầấẩẫậeèéẻẽẹêềếểễệiìíỉĩịoòóỏõọôồốổỗộơờớởỡợuùúủũụưừứửữựyỳýỷỹỵ";

        for (String word : words) {
            // Nếu từ đó chứa chữ f nhưng không đi kèm với p (không phải 'ph') -> Chặn (Ví dụ: "fanta", "kaff")
            if (word.contains("f") && !word.contains("ph")) return false;

            String cleanWord = java.text.Normalizer.normalize(word, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");

            if (cleanWord.matches(".*[bcdđghklmnpqrstvx]{4,}.*")) return false;

            boolean hasVowel = false;
            for (char c : word.toCharArray()) {
                if (vowels.indexOf(c) != -1) {
                    hasVowel = true;
                    break;
                }
            }
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

    public String getDoctorCode() { return doctorCode; }
    public void setDoctorCode(String doctorCode) { this.doctorCode = doctorCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public Integer getCapacityLimit() { return capacityLimit; }
    public void setCapacityLimit(Integer capacityLimit) { this.capacityLimit = capacityLimit; }
}