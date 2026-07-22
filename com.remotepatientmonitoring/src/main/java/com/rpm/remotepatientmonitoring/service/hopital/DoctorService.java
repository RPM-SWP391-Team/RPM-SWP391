package com.rpm.remotepatientmonitoring.service.hopital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.dto.hopital.DoctorEditDTO;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private AlertRepository alertRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

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

        Map<String, Object> oldLog = new HashMap<>();
        oldLog.put("status", "INACTIVE");
        Map<String, Object> newLog = new HashMap<>();
        newLog.put("status", "ACTIVE");

        try {
            String oldVal = objectMapper.writeValueAsString(oldLog);
            String newVal = objectMapper.writeValueAsString(newLog);
            saveAuditLog("ACTIVATE_DOCTOR", "doctors", doctor.getId(), oldVal, newVal, "Tái kích hoạt tài khoản bác sĩ");
        } catch (Exception e) {
            saveAuditLog("ACTIVATE_DOCTOR", "doctors", doctor.getId(), "{\"status\":\"INACTIVE\"}", "{\"status\":\"ACTIVE\"}", "Tái kích hoạt tài khoản bác sĩ");
        }
    }

    /**
     * 1. TẠO MỚI BÁC SĨ (Lưu vết thông tin vào Log)
     */
    @Transactional
    public Doctor createDoctor(Integer hospitalId, String doctorCode, String fullName, String phone,
                               String email, String gender, java.time.LocalDate dateOfBirth, String password, String specialty, Integer capacityLimit) {

        String nameRegex = "^[\\p{L}\\s]{2,50}$";
        if (fullName == null || !fullName.trim().matches(nameRegex)) {
            throw new IllegalArgumentException("Họ và tên bác sĩ không hợp lệ. Tên chỉ được phép chứa chữ cái tiếng Việt và khoảng trắng.");
        }

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

        Doctor doctor = Doctor.builder()
                .account(account)
                .hospital(hospital)
                .doctorCode(doctorCode)
                .fullName(fullName)
                .phone(phone)
                .gender(gender)
                .dateOfBirth(dateOfBirth)
                .specialty(specialty)
                .capacityLimit(capacityLimit)
                .currentPatientCount(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        doctor = doctorRepository.save(doctor);

        Map<String, Object> newLog = new HashMap<>();
        newLog.put("accountId", account.getId());
        newLog.put("doctorCode", doctor.getDoctorCode());
        newLog.put("fullName", doctor.getFullName());
        newLog.put("email", email);
        newLog.put("phone", phone);
        newLog.put("gender", gender);
        newLog.put("dateOfBirth", dateOfBirth);
        newLog.put("specialty", doctor.getSpecialty());
        newLog.put("capacityLimit", capacityLimit);
        newLog.put("isActive", doctor.getIsActive());

        try {
            String newVal = objectMapper.writeValueAsString(newLog);
            saveAuditLog("CREATE_DOCTOR", "doctors", doctor.getId(), null, newVal, "Tạo mới tài khoản bác sĩ");
        } catch (Exception e) {
            saveAuditLog("CREATE_DOCTOR", "doctors", doctor.getId(), null, "{\"error\":\"Parse JSON lỗi\"}", "Tạo mới tài khoản bác sĩ");
        }

        try {
            boolean isMailSent = emailService.sendDoctorPassword(email, fullName, finalPassword);
            if (!isMailSent) {
                throw new IllegalArgumentException("Email lỗi: Địa chỉ email không tồn tại hoặc không thể chuyển phát thư.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Email lỗi: Địa chỉ email không khả dụng hoặc cấu hình SMTP Google Mail bị từ chối.");
        }

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
     * 2. VÔ HIỆU HÓA BÁC SĨ & ĐIỀU CHUYỂN BỆNH NHÂN
     */
    @Transactional
    public void deactivateDoctor(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + doctorId));

        List<Patient> activePatients = patientRepository.findByDoctorIdAndIsActiveTrue(doctorId);

        List<Map<String, Object>> transferredPatientsInfo = new ArrayList<>();
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
                    throw new IllegalStateException("Cạn kiệt hạn ngạch tiếp nhận bệnh nhân của bệnh viện!");
                }

                ReplacementDoctorDto targetDto = replacements.get(replacementIndex);
                Doctor replacementDoctor = targetDto.getDoctor();

                // 2.1. Điều chuyển bệnh nhân
                patient.setDoctor(replacementDoctor);
                patient.setUpdatedAt(LocalDateTime.now());
                patientRepository.save(patient);

                // 2.2. Chuyển giao cảnh báo chưa xử lý
                List<Alert> unresolvedAlerts = alertRepository.findByPatientIdAndIsResolvedFalse(patient.getId());
                for (Alert alert : unresolvedAlerts) {
                    alert.setDoctor(replacementDoctor);
                    alertRepository.save(alert);
                }

                // 2.3. Hủy lịch hẹn tương lai
                List<Appointment> pendingAppointments = appointmentRepository.findByPatientIdAndDoctorIdAndStatusIn(
                        patient.getId(), doctorId, List.of("PENDING", "ACCEPTED"));

                for (Appointment app : pendingAppointments) {
                    app.setStatus("CANCELLED");
                    app.setRejectionReason("Bác sĩ phụ trách hiện ngừng hoạt động tại viện. Vui lòng đặt lại lịch khám mới với BS. " + replacementDoctor.getFullName());
                    app.setUpdatedAt(LocalDateTime.now());
                    appointmentRepository.save(app);
                }

                // 2.4. Gửi thông báo hệ thống In-App cho Bệnh nhân
                try {
                    Notification notif = Notification.builder()
                            .recipientType("PATIENT")
                            .recipientId(patient.getId())
                            .notificationType("SYSTEM_UPDATE")
                            .title("Thay đổi Bác sĩ phụ trách")
                            .content("Bác sĩ phụ trách của bạn đã được thay đổi thành BS. " + replacementDoctor.getFullName() + ". Các lịch hẹn cũ đã bị hủy, vui lòng đặt lại lịch mới.")
                            .channel("IN_APP")
                            .status("PENDING")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notif);
                } catch (Exception e) {
                    System.err.println("Cảnh báo: Bảng Notification chưa tương thích hoặc chưa được định cấu hình.");
                }

                // Đóng gói thông tin log chi tiết (ĐÃ BỔ SUNG THÊM TÊN BỆNH NHÂN)
                Map<String, Object> pInfo = new HashMap<>();
                pInfo.put("patientId", patient.getId());
                pInfo.put("patientCode", patient.getPatientCode() != null ? patient.getPatientCode() : "Chưa có mã");
                pInfo.put("patientName", patient.getFullName()); // Bổ sung để hiển thị ngoài HTML
                pInfo.put("newDoctorId", replacementDoctor.getId());
                pInfo.put("newDoctorName", replacementDoctor.getFullName());
                transferredPatientsInfo.add(pInfo);

                if (!assignedReplacementDoctorIds.contains(replacementDoctor.getId())) {
                    assignedReplacementDoctorIds.add(replacementDoctor.getId());
                }

                targetDto.setTempCount(targetDto.getTempCount() + 1);
                replacementDoctor.setCurrentPatientCount(targetDto.getTempCount());
                doctorRepository.save(replacementDoctor);
            }
        }

        // Vô hiệu hóa tài khoản bác sĩ cũ
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

        // Tạo log Audit Trail dạng JSON (ĐÃ BỔ SUNG THÊM TÊN BÁC SĨ CŨ)
        Map<String, Object> newLog = new HashMap<>();
        newLog.put("status", "INACTIVE");
        newLog.put("oldDoctorName", doctor.getFullName()); // Bổ sung để hiển thị ngoài HTML
        newLog.put("replacementDoctorIds", assignedReplacementDoctorIds);
        newLog.put("transfers", transferredPatientsInfo);

        try {
            String newVal = objectMapper.writeValueAsString(newLog);
            saveAuditLog("ASSIGN_PATIENTS", "doctors", doctor.getId(), "{\"status\":\"ACTIVE\"}", newVal, "Vô hiệu hóa bác sĩ & điều chuyển thông tin theo dõi bệnh án");
        } catch (Exception e) {
            saveAuditLog("ASSIGN_PATIENTS", "doctors", doctor.getId(), "{\"status\":\"ACTIVE\"}", "{\"status\":\"INACTIVE\"}", "Vô hiệu hóa bác sĩ & điều chuyển thông tin theo dõi bệnh án");
        }
    }

    public Page<Doctor> searchAndFilterAllDoctors(String keyword, String specialty, Pageable pageable) {
        return searchAndFilterAllDoctors(keyword, specialty, null, pageable);
    }

    public Page<Doctor> searchAndFilterAllDoctors(String keyword, String specialty, String status, Pageable pageable) {
        String cleanKeyword = (keyword != null) ? keyword.trim() : "";
        String cleanSpecialty = (specialty != null) ? specialty.trim() : "";
        String cleanStatus = (status != null) ? status.trim().toUpperCase() : "";

        if (cleanKeyword.length() > 100) {
            cleanKeyword = cleanKeyword.substring(0, 100);
        }

        return doctorRepository.searchAndFilterDoctors(cleanKeyword, cleanSpecialty, cleanStatus, pageable);
    }

    public Doctor getDoctorById(int id) {
        return doctorRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + id));
    }

    public List<Patient> getPatientsByDoctorId(int doctorId) {
        return patientRepository.findByDoctorIdAndIsActiveTrue(doctorId);
    }

    /**
     * 3. CẬP NHẬT THÔNG TIN BÁC SĨ
     */
    @Transactional
    public void updateDoctor(int id, DoctorEditDTO dto) {
        Doctor doctor = doctorRepository.findById(Integer.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + id));

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

        Account account = doctor.getAccount();
        String currentEmail = account.getEmail();
        String newEmail = dto.getEmail().trim();
        boolean isEmailChanged = !currentEmail.equalsIgnoreCase(newEmail);
        String newPassword = null;

        if (isEmailChanged) {
            if (accountRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email này đã được sử dụng bởi một tài khoản khác.");
            }
            newPassword = generatePassword();
            try {
                boolean isMailSent = emailService.sendDoctorPassword(newEmail, dto.getFullName(), newPassword);
                if (!isMailSent) {
                    throw new IllegalArgumentException("Email lỗi: Địa chỉ email mới không tồn tại hoặc không thể chuyển phát thư.");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Email lỗi: Địa chỉ email mới không khả dụng hoặc cấu hình SMTP bị từ chối.");
            }
        }

        // Sao chụp thông tin cũ
        Map<String, Object> oldLog = new HashMap<>();
        oldLog.put("email", currentEmail);
        oldLog.put("fullName", doctor.getFullName());
        oldLog.put("phone", doctor.getPhone());
        oldLog.put("gender", doctor.getGender());
        oldLog.put("dateOfBirth", doctor.getDateOfBirth());
        oldLog.put("specialty", doctor.getSpecialty());
        oldLog.put("capacityLimit", doctor.getCapacityLimit());

        Map<String, Object> newLog = new HashMap<>();
        boolean isChanged = false;

        if (isEmailChanged) {
            newLog.put("email", newEmail);
            newLog.put("password", "Đã cấp lại tự động qua email mới");
            account.setEmail(newEmail);
            account.setPasswordHash(passwordEncoder.encode(newPassword));
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
            isChanged = true;
        }

        if (!Objects.equals(doctor.getFullName(), dto.getFullName().trim())) {
            newLog.put("fullName", dto.getFullName().trim());
            doctor.setFullName(dto.getFullName().trim());
            isChanged = true;
        }

        if (!Objects.equals(doctor.getPhone(), dto.getPhone().trim())) {
            newLog.put("phone", dto.getPhone().trim());
            doctor.setPhone(dto.getPhone().trim());
            isChanged = true;
        }

        if (!Objects.equals(doctor.getGender(), dto.getGender())) {
            newLog.put("gender", dto.getGender());
            doctor.setGender(dto.getGender());
            isChanged = true;
        }

        if (!Objects.equals(doctor.getDateOfBirth(), dto.getDateOfBirth())) {
            newLog.put("dateOfBirth", dto.getDateOfBirth());
            doctor.setDateOfBirth(dto.getDateOfBirth());
            isChanged = true;
        }

        if (!Objects.equals(doctor.getSpecialty(), dto.getSpecialty())) {
            newLog.put("specialty", dto.getSpecialty());
            doctor.setSpecialty(dto.getSpecialty());
            isChanged = true;
        }

        if (!Objects.equals(doctor.getCapacityLimit(), dto.getCapacityLimit())) {
            newLog.put("capacityLimit", dto.getCapacityLimit());
            doctor.setCapacityLimit(dto.getCapacityLimit());
            isChanged = true;
        }

        if (isChanged) {
            doctor.setUpdatedAt(LocalDateTime.now());
            doctorRepository.save(doctor);

            try {
                String oldVal = objectMapper.writeValueAsString(oldLog);
                String newVal = objectMapper.writeValueAsString(newLog);
                saveAuditLog("UPDATE_DOCTOR", "doctors", doctor.getId(), oldVal, newVal,
                        isEmailChanged ? "Cập nhật thông tin và đổi Email/Mật khẩu" : "Cập nhật thông tin bác sĩ");
            } catch (Exception e) {
                System.err.println("Lỗi parse JSON Audit Log");
            }
        }
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

    private void saveAuditLog(String action, String targetTable, Integer targetRecordId, String oldValue, String newValue, String notes) {
        try {
            Integer adminId = 0;
            String adminEmail = "HOSPITAL_ADMIN";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                Object principal = auth.getPrincipal();
                if (principal instanceof com.rpm.remotepatientmonitoring.config.CustomUserDetails) {
                    Account acc = ((com.rpm.remotepatientmonitoring.config.CustomUserDetails) principal).getAccount();
                    adminId = acc.getId();
                    adminEmail = acc.getEmail();
                }
            }

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
                    .actorType("HOSPITAL_ADMIN")
                    .actorId(adminId)
                    .action(action)
                    .targetTable(targetTable)
                    .targetRecordId(targetRecordId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .notes(finalNotes)
                    .build();

            auditTrailRepository.save(log);
        } catch (Exception e) {
            System.err.println("Cảnh báo: Lỗi ghi log System (" + action + "): " + e.getMessage());
        }
    }
}