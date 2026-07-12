package com.rpm.remotepatientmonitoring.service.hopital;

import com.rpm.remotepatientmonitoring.dto.hopital.DoctorEditDTO;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.AuditTrail; // Đảm bảo import đúng Entity AuditTrail của bạn
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

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

        String newValueJson = "{\"status\":\"ACTIVE\"}";
        saveAuditLog("ACTIVATE_DOCTOR", "doctors", doctor.getId(), "{\"status\":\"INACTIVE\"}", newValueJson, "Tái kích hoạt tài khoản bác sĩ");
    }

    /**
     * 1. TẠO MỚI BÁC SĨ (Đã tích hợp ghi Audit Log an toàn)
     */
    @Transactional
    public Doctor createDoctor(Integer hospitalId, String doctorCode, String fullName, String phone,
                               String email, String gender, String password, String specialty, Integer capacityLimit) {

        // Giữ nguyên logic validation họ và tên gốc của bạn
        String nameRegex = "^[\\p{L}\\s]{2,50}$";
        if (fullName == null || !fullName.trim().matches(nameRegex)) {
            throw new IllegalArgumentException("Họ và tên bác sĩ không hợp lệ. Tên chỉ được phép chứa chữ cái tiếng Việt và khoảng trắng.");
        }

        // Giữ nguyên logic check trùng dữ liệu của bạn
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

        String finalPassword = generatePassword();

        // Giữ nguyên logic kiểm tra gửi Mail SMTP thực tế của bạn
        try {
            boolean isMailSent = emailService.sendDoctorPassword(email, fullName, finalPassword);
            if (!isMailSent) {
                throw new IllegalArgumentException("Email lỗi: Địa chỉ email không tồn tại hoặc không thể chuyển phát thư.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Email lỗi: Địa chỉ email không khả dụng hoặc cấu hình SMTP Google Mail bị từ chối.");
        }

        // Tạo tài khoản hệ thống (Account)
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

        // Tạo thông tin bác sĩ (Doctor)
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

        // ================= THÊM MỚI: AUDIT LOG CHO HÀNH ĐỘNG TẠO BÁC SĨ =================
        // ... Code lưu bác sĩ ở trên ...
        String newValueJson = String.format(
                "{\"doctorCode\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"specialty\":\"%s\",\"capacityLimit\":%d}",
                doctor.getDoctorCode() != null ? doctor.getDoctorCode() : "",
                doctor.getFullName() != null ? doctor.getFullName() : "",
                email,
                doctor.getSpecialty() != null ? doctor.getSpecialty() : "",
                capacityLimit
        );
        saveAuditLog("CREATE_DOCTOR", "doctors", doctor.getId(), "-", newValueJson, "Tạo mới tài khoản bác sĩ");

        return doctor;
    }

    public String generateNextDoctorCode() {
        String latestCode = doctorRepository.findLatestDoctorCode();
        if (latestCode == null || latestCode.trim().isEmpty()) {
            return "BS001";
        }
        try {
            String numberPart = latestCode.substring(2);
            int nextNumber = Integer.parseInt(numberPart) + 1;
            return String.format("BS%03d", nextNumber);
        } catch (Exception e) {
            return "BS" + (int)(Math.random() * 900 + 100);
        }
    }

    /**
     * 2. VÔ HIỆU HÓA BÁC SĨ & ĐIỀU CHUYỂN BỆNH NHÂN (Đã tích hợp ghi Audit Log mượt mà)
     */
    @Transactional
    public void deactivateDoctor(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + doctorId));

        List<Patient> activePatients = patientRepository.findByDoctorIdAndIsActiveTrue(doctorId);

        // Biến vệ tinh thu thập thông tin log (Không can thiệp cấu trúc điều hướng tải)
        List<Integer> transferredPatientIds = new ArrayList<>();
        List<Integer> assignedReplacementDoctorIds = new ArrayList<>();

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

                // Giữ nguyên logic cập nhật bệnh nhân của bạn
                patient.setDoctor(replacementDoctor);
                patient.setUpdatedAt(LocalDateTime.now());
                patientRepository.save(patient);

                // Thu thập thông tin ID phục vụ lưu vết nhật ký
                transferredPatientIds.add(patient.getId());
                if (!assignedReplacementDoctorIds.contains(replacementDoctor.getId())) {
                    assignedReplacementDoctorIds.add(replacementDoctor.getId());
                }

                // Giữ nguyên logic cập nhật tải bác sĩ thay thế của bạn
                targetDto.setTempCount(targetDto.getTempCount() + 1);
                replacementDoctor.setCurrentPatientCount(targetDto.getTempCount());
                doctorRepository.save(replacementDoctor);
            }
        }

        // Giữ nguyên luồng vô hiệu hóa bác sĩ gốc của bạn
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

        // ================= THÊM MỚI: AUDIT LOG CHO HÀNH ĐỘNG VÔ HIỆU HÓA & ĐIỀU CHUYỂN =================
        String newValueJson = String.format(
                "{\"status\":\"INACTIVE\",\"doctorId\":%s,\"patientIds\":%s}",
                assignedReplacementDoctorIds.toString(),
                transferredPatientIds.toString()
        );
        saveAuditLog("ASSIGN_PATIENTS", "doctors", doctor.getId(), "{\"status\":\"ACTIVE\"}", newValueJson, "Vô hiệu hóa bác sĩ & điều chuyển bệnh nhân");
    }

    public Page<Doctor> searchAndFilterAllDoctors(String keyword, String specialty, Pageable pageable) {
        String cleanKeyword = (keyword != null) ? keyword.trim() : "";
        String cleanSpecialty = (specialty != null) ? specialty.trim() : "";

        if (cleanKeyword.length() > 100) {
            cleanKeyword = cleanKeyword.substring(0, 100);
        }

        return doctorRepository.searchAndFilterDoctors(cleanKeyword, cleanSpecialty, pageable);
    }

    public Doctor getDoctorById(int id) {
        return doctorRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + id));
    }

    public List<Patient> getPatientsByDoctorId(int doctorId) {
        return patientRepository.findByDoctorIdAndIsActiveTrue(doctorId);
    }

    /**
     * 3. CẬP NHẬT THÔNG TIN BÁC SĨ (Đã tích hợp chụp ảnh Snapshot Cũ - Mới)
     */
    @Transactional
    public void updateDoctor(int id, DoctorEditDTO dto) {
        Doctor doctor = doctorRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + id));

        // Giữ nguyên logic bẫy trùng SĐT và bẫy giới hạn tải của bạn
        if (doctorRepository.existsByPhoneAndIdNot(dto.getPhone(), id)) {
            throw new IllegalArgumentException("Số điện thoại đã được đăng ký bởi một bác sĩ khác.");
        }

        if (dto.getCapacityLimit() < doctor.getCurrentPatientCount()) {
            throw new IllegalArgumentException("Giới hạn tải không thể nhỏ hơn số lượng bệnh nhân hiện tại bác sĩ đang phụ trách (" + doctor.getCurrentPatientCount() + " bệnh nhân).");
        }

        String nameRegex = "^[\\p{L}\\s]{2,50}$";
        if (dto.getFullName() == null || !dto.getFullName().trim().matches(nameRegex)) {
            throw new IllegalArgumentException("Họ và tên bác sĩ không hợp lệ. Tên chỉ được phép chứa chữ cái tiếng Việt và khoảng trắng.");
        }

        // ================= STEP 1: CHỤP ẢNH DỮ LIỆU CŨ TRƯỚC KHI THAY ĐỔI =================
        String oldValueJson = "-";
        try {
            oldValueJson = String.format(
                    "{\"fullName\": \"%s\", \"phone\": \"%s\", \"gender\": \"%s\", \"specialty\": \"%s\", \"capacityLimit\": %d}",
                    doctor.getFullName() != null ? doctor.getFullName() : "",
                    doctor.getPhone() != null ? doctor.getPhone() : "",
                    doctor.getGender() != null ? doctor.getGender() : "",
                    doctor.getSpecialty() != null ? doctor.getSpecialty() : "",
                    doctor.getCapacityLimit() != null ? doctor.getCapacityLimit() : 0
            );
        } catch (Exception e) {
            oldValueJson = "{\"error\": \"Không thể lưu ảnh snapshot dữ liệu cũ\"}";
        }

        // Giữ nguyên luồng cập nhật gán dữ liệu gốc của bạn
        doctor.setFullName(dto.getFullName().trim());
        doctor.setPhone(dto.getPhone().trim());
        doctor.setGender(dto.getGender());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setCapacityLimit(dto.getCapacityLimit());
        doctor.setUpdatedAt(LocalDateTime.now());

        doctorRepository.save(doctor);

        // ================= STEP 2: GHI AUDIT LOG LƯU GIÁ TRỊ MỚI =================
        String newValueJson = String.format(
                "{\"fullName\":\"%s\",\"phone\":\"%s\",\"gender\":\"%s\",\"specialty\":\"%s\",\"capacityLimit\":%d}",
                doctor.getFullName(),
                doctor.getPhone(),
                doctor.getGender(),
                doctor.getSpecialty(),
                doctor.getCapacityLimit()
        );
        saveAuditLog("UPDATE_DOCTOR", "doctors", doctor.getId(), oldValueJson, newValueJson, "Cập nhật thông tin bác sĩ");
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

    // Thêm 2 hàm này vào cuối class DoctorService của bạn

    private void saveAuditLog(String action, String targetTable, Integer targetRecordId, String oldValue, String newValue, String notes) {
        try {
            Integer adminId = 0; // Default ID phòng hờ
            String adminEmail = "HOSPITAL_ADMIN";

            // 1. Trích xuất Admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof com.rpm.remotepatientmonitoring.config.CustomUserDetails) {
                    Account acc = ((com.rpm.remotepatientmonitoring.config.CustomUserDetails) principal).getAccount();
                    adminId = acc.getId();
                    adminEmail = acc.getEmail();
                }
            }

            // 2. Trích xuất IP & Thiết bị
            String ipAddress = "Unknown";
            String deviceInfo = "Unknown";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    deviceInfo = request.getHeader("User-Agent");
                    if (deviceInfo != null && deviceInfo.length() > 250) {
                        deviceInfo = deviceInfo.substring(0, 250);
                    }

                    ipAddress = request.getHeader("X-Forwarded-For");
                    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                        ipAddress = request.getRemoteAddr();
                    }
                    if (ipAddress != null && ipAddress.contains(",")) {
                        ipAddress = ipAddress.split(",")[0].trim();
                    }
                }
            } catch (Exception e) {}

            String finalNotes = notes + " (Bởi: " + adminEmail + ")";

            AuditTrail log = AuditTrail.builder()
                    .actorType("HOSPITAL_ADMIN") // ĐÚNG CHUẨN DATABASE
                    .actorId(adminId)            // LẤY ID THỰC
                    .action(action)
                    .targetTable(targetTable)
                    .targetRecordId(targetRecordId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .deviceInfo(deviceInfo)
                    .notes(finalNotes)
                    // createdAt sẽ được @PrePersist tự động lo
                    .build();

            auditTrailRepository.save(log);
        } catch (Exception e) {
            System.err.println("Cảnh báo: Lỗi ghi log System (" + action + "): " + e.getMessage());
        }
    }
}