package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.dto.DoctorEditDTO;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // SỬA CHUẨN: Bỏ hoàn toàn (required = false) để ép hệ thống kiểm tra Bean EmailService chặt chẽ khi startup
    @Autowired
    private EmailService emailService;

    private String generatePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public List<Doctor> getDoctorsByHospital(Integer hospitalId) {
        return doctorRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public void activateDoctor(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + doctorId));

        if (doctor.getIsActive()) {
            throw new IllegalStateException("Tài khoản bác sĩ này hiện đã ở trạng thái hoạt động.");
        }

        doctor.setIsActive(true);
        doctor.setCurrentPatientCount(0);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        if (doctor.getAccount() != null) {
            Account account = doctor.getAccount();
            account.setIsActive(true);
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
        }
    }

    @Transactional
    public Doctor createDoctor(Integer hospitalId, String doctorCode, String fullName, String phone,
                               String email,String gender, String password, String specialty, Integer capacityLimit) {

        // 1. Kiểm tra định dạng họ và tên tầng Service tránh lọt dữ liệu bừa bãi
        String nameRegex = "^[\\p{L}\\s]{2,50}$";
        if (fullName == null || !fullName.trim().matches(nameRegex)) {
            throw new IllegalArgumentException("Họ và tên bác sĩ không hợp lệ. Tên chỉ được phép chứa chữ cái tiếng Việt và khoảng trắng.");
        }

        // 2. Kiểm tra trùng lặp dữ liệu hệ thống (Chỉ check định danh độc nhất, không chặn họ tên trùng thực tế)
        if (doctorRepository.existsByDoctorCode(doctorCode)) {
            throw new IllegalArgumentException("Mã bác sĩ đã tồn tại trong hệ thống.");
        }
        if (doctorRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Số điện thoại đã được đăng ký.");
        }
        if (accountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được đăng ký tài khoản.");
        }

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh viện với ID: " + hospitalId));

        // Tự động sinh mật khẩu ngẫu nhiên
        String finalPassword = generatePassword();

        // 🌟 BƯỚC 3: GỬI MAIL KIỂM TRA ĐỊA CHỈ THỰC TẾ TRƯỚC KHI GHI NHẬN XUỐNG DB
        try {
            boolean isMailSent = emailService.sendDoctorPassword(email, fullName, finalPassword);
            if (!isMailSent) {
                throw new IllegalArgumentException("Email lỗi: Địa chỉ email không tồn tại hoặc không thể chuyển phát thư.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Email lỗi: Địa chỉ email không khả dụng hoặc cấu hình SMTP Google Mail bị từ chối.");
        }

        // 4. Tạo tài khoản hệ thống (Account)
        Account account = Account.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(finalPassword))
                .role("DOCTOR")
                .isEmailVerified(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        // 5. Tạo thông tin bác sĩ (Doctor)
        Doctor doctor = Doctor.builder()
                .account(account)
                .hospital(hospital)
                .doctorCode(doctorCode)
                .fullName(fullName)
                .phone(phone)
                .gender(gender)
                .specialty(specialty)
                .capacityLimit(capacityLimit)
                .currentPatientCount(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doctor = doctorRepository.save(doctor);

        return doctor;
    }

    // Thêm duy nhất hàm này vào bên trong class DoctorService.java của bạn
    public String generateNextDoctorCode() {
        String latestCode = doctorRepository.findLatestDoctorCode();
        if (latestCode == null || latestCode.trim().isEmpty()) {
            return "BS001"; // Khởi tạo mã đầu tiên nếu DB trống
        }
        try {
            // "BS005" -> substring(2) cắt bỏ 2 chữ đầu để lấy chuỗi "005"
            String numberPart = latestCode.substring(2);
            int nextNumber = Integer.parseInt(numberPart) + 1;
            return String.format("BS%03d", nextNumber); // Tự động bù số 0 thành: BS006
        } catch (Exception e) {
            return "BS" + (int)(Math.random() * 900 + 100); // Dự phòng an toàn nếu parse lỗi
        }
    }

    @Transactional
    public void deactivateDoctor(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + doctorId));

        List<Patient> activePatients = patientRepository.findByDoctorIdAndIsActiveTrue(doctorId);

        if (!activePatients.isEmpty()) {
            List<ReplacementDoctorDto> replacements = getReplacementCapacityList(doctor.getHospital().getId(), doctorId);

            if (replacements.isEmpty()) {
                throw new IllegalStateException("Không thể vô hiệu hóa! Toàn bộ bác sĩ khác trong viện đều đã QUÁ TẢI.");
            }

            int replacementIndex = 0;
            for (Patient patient : activePatients) {
                while (replacementIndex < replacements.size() &&
                        replacements.get(replacementIndex).getTempCount() >= replacements.get(replacementIndex).getCapacityLimit()) {
                    replacementIndex++;
                }

                if (replacementIndex >= replacements.size()) {
                    throw new IllegalStateException("Cạn kiệt hạn ngạch tiếp nhận!");
                }

                ReplacementDoctorDto targetDto = replacements.get(replacementIndex);
                Doctor replacementDoctor = targetDto.getDoctor();

                patient.setDoctor(replacementDoctor);
                patient.setUpdatedAt(LocalDateTime.now());
                patientRepository.save(patient);

                targetDto.setTempCount(targetDto.getTempCount() + 1);
                replacementDoctor.setCurrentPatientCount(targetDto.getTempCount());
                doctorRepository.save(replacementDoctor);
            }
        }

        doctor.setCurrentPatientCount(0);
        doctor.setIsActive(false);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        if (doctor.getAccount() != null) {
            Account account = doctor.getAccount();
            account.setIsActive(false);
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
        }
    }

    /// Hàm xử lý nghiệp vụ bộ lọc kép: Làm sạch dữ liệu đầu vào và gọi Repository
    public List<Doctor> searchAndFilterAllDoctors(String keyword, String specialty) {
        String cleanKeyword = (keyword != null) ? keyword.trim() : "";
        String cleanSpecialty = (specialty != null) ? specialty.trim() : "";

        // Giới hạn độ dài chuỗi tìm kiếm tối đa 100 ký tự để bảo vệ hiệu năng hệ thống
        if (cleanKeyword.length() > 100) {
            cleanKeyword = cleanKeyword.substring(0, 100);
        }

        // Đẩy xuống Database xử lý lọc phân tầng kèm sắp xếp tự động chuẩn chỉ
        return doctorRepository.searchAndFilterDoctors(cleanKeyword, cleanSpecialty);
    }

    public Doctor getDoctorById(int id) {
        return doctorRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + id));
    }

    public List<Patient> getPatientsByDoctorId(int doctorId) {
        return patientRepository.findByDoctorIdAndIsActiveTrue(doctorId);
    }

    @Transactional
    public void updateDoctor(int id, DoctorEditDTO dto) {
        Doctor doctor = doctorRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + id));

        // Bẫy trùng SĐT: loại trừ ID của chính bác sĩ đang sửa
        if (doctorRepository.existsByPhoneAndIdNot(dto.getPhone(), id)) {
            throw new IllegalArgumentException("Số điện thoại đã được đăng ký bởi một bác sĩ khác.");
        }

        // Bẫy Giới hạn tải (capacity_limit): Giới hạn mới không được nhỏ hơn số lượng bệnh nhân thực tế hiện tại
        if (dto.getCapacityLimit() < doctor.getCurrentPatientCount()) {
            throw new IllegalArgumentException("Giới hạn tải không thể nhỏ hơn số lượng bệnh nhân hiện tại bác sĩ đang phụ trách (" + doctor.getCurrentPatientCount() + " bệnh nhân).");
        }

        // Kiểm tra định dạng họ và tên bác sĩ tầng Service
        String nameRegex = "^[\\p{L}\\s]{2,50}$";
        if (dto.getFullName() == null || !dto.getFullName().trim().matches(nameRegex)) {
            throw new IllegalArgumentException("Họ và tên bác sĩ không hợp lệ. Tên chỉ được phép chứa chữ cái tiếng Việt và khoảng trắng.");
        }

        // Gán dữ liệu sửa đổi
        doctor.setFullName(dto.getFullName().trim());
        doctor.setPhone(dto.getPhone().trim());
        doctor.setGender(dto.getGender());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setCapacityLimit(dto.getCapacityLimit());
        doctor.setUpdatedAt(LocalDateTime.now());

        doctorRepository.save(doctor);
    }

    private List<ReplacementDoctorDto> getReplacementCapacityList(Integer hospitalId, Integer currentDoctorId) {
        List<com.rpm.remotepatientmonitoring.model.Doctor> docs = doctorRepository.findBestReplacementDoctors(hospitalId, currentDoctorId);
        return docs.stream()
                .map(d -> new ReplacementDoctorDto(d, d.getCurrentPatientCount(), d.getCapacityLimit()))
                .collect(Collectors.toList());
    }

    private static class ReplacementDoctorDto {
        private final Doctor doctor;
        private int tempCount;
        private final int capacityLimit;

        public ReplacementDoctorDto(Doctor doctor, int tempCount, int capacityLimit) {
            this.doctor = doctor;
            this.tempCount = tempCount;
            this.capacityLimit = capacityLimit;
        }
        public Doctor getDoctor() { return doctor; }
        public int getTempCount() { return tempCount; }
        public void setTempCount(int tempCount) { this.tempCount = tempCount; }
        public int getCapacityLimit() { return capacityLimit; }
    }
}