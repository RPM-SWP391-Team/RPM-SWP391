package com.rpm.remotepatientmonitoring.service.hospital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.dto.hospital.HospitalProfileDTO;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import com.rpm.remotepatientmonitoring.model.HospitalAdmin;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalAdminRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class HospitalProfileService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HospitalAdminRepository hospitalAdminRepository;

    public HospitalProfileDTO getProfileByAccountId(Integer accountId) {
        java.util.Optional<HospitalAdmin> adminOpt = hospitalAdminRepository.findByAccountId(accountId);
        if (!adminOpt.isPresent()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin quản trị viên bệnh viện.");
        }
        HospitalAdmin admin = adminOpt.get();

        Hospital hospital = admin.getHospital();
        Account account = admin.getAccount();

        return HospitalProfileDTO.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .hospitalCode(hospital.getHospitalCode())
                .fullName(hospital.getFullName())
                .address(hospital.getAddress())
                .phone(hospital.getPhone())
                .adminCode(admin.getAdminCode())
                .adminFullName(admin.getFullName())
                .adminDepartment(admin.getDepartment())
                .adminPosition(admin.getPosition())
                .adminPhone(admin.getPhone())
                .adminRoleType(admin.getAdminRoleType())
                .build();
    }

    @Transactional
    public void updateProfile(Integer accountId, HospitalProfileDTO dto) {
        java.util.Optional<HospitalAdmin> adminOpt = hospitalAdminRepository.findByAccountId(accountId);
        if (!adminOpt.isPresent()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin quản trị viên bệnh viện.");
        }
        HospitalAdmin admin = adminOpt.get();

        Hospital hospital = admin.getHospital();
        Account account = admin.getAccount();

        // -------------------------------------------------------------
        // TẠO SNAPSHOT DỮ LIỆU CŨ ĐỂ GHI LOG
        // -------------------------------------------------------------
        Map<String, Object> oldLog = new HashMap<>();
        oldLog.put("email", account.getEmail());
        oldLog.put("fullName", hospital.getFullName());
        oldLog.put("address", hospital.getAddress());
        oldLog.put("phone", hospital.getPhone());
        oldLog.put("adminFullName", admin.getFullName());
        oldLog.put("adminDepartment", admin.getDepartment());
        oldLog.put("adminPosition", admin.getPosition());
        oldLog.put("adminPhone", admin.getPhone());

        Map<String, Object> newLog = new HashMap<>(oldLog);
        boolean isChanged = false;

        // 1. Xử lý cập nhật Email
        String newEmail = dto.getEmail().trim();
        if (!account.getEmail().equalsIgnoreCase(newEmail)) {
            if (accountRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email này đã được sử dụng bởi tài khoản khác.");
            }
            newLog.put("email", newEmail);
            account.setEmail(newEmail);
            isChanged = true;
        }

        // 2. Xử lý cập nhật Tên hiển thị bệnh viện
        String newFullName = dto.getFullName() != null ? dto.getFullName().trim() : "";
        if (newFullName.isEmpty()) {
            throw new IllegalArgumentException("Tên hiển thị bệnh viện không được để trống.");
        }
        if (!newFullName.equals(hospital.getFullName())) {
            newLog.put("fullName", newFullName);
            hospital.setFullName(newFullName);
            isChanged = true;
        }

        // 3. Xử lý cập nhật Địa chỉ
        String newAddress = dto.getAddress() != null ? dto.getAddress().trim() : "";
        if (!newAddress.equals(hospital.getAddress() != null ? hospital.getAddress().trim() : "")) {
            newLog.put("address", newAddress);
            hospital.setAddress(newAddress.isEmpty() ? null : newAddress);
            isChanged = true;
        }

        // 4. Xử lý cập nhật Số điện thoại hotline bệnh viện
        String newPhone = dto.getPhone() != null ? dto.getPhone().trim() : "";
        if (!newPhone.equals(hospital.getPhone() != null ? hospital.getPhone().trim() : "")) {
            newLog.put("phone", newPhone);
            hospital.setPhone(newPhone.isEmpty() ? null : newPhone);
            isChanged = true;
        }

        // 4.5. Xử lý cập nhật Tên hiển thị Quản trị viên
        String newAdminFullName = dto.getAdminFullName() != null ? dto.getAdminFullName().trim() : "";
        if (newAdminFullName.isEmpty()) {
            throw new IllegalArgumentException("Họ tên quản trị viên không được để trống.");
        }
        if (!newAdminFullName.equals(admin.getFullName())) {
            newLog.put("adminFullName", newAdminFullName);
            admin.setFullName(newAdminFullName);
            isChanged = true;
        }

        // 4.6. Xử lý cập nhật Phòng ban
        String newDept = dto.getAdminDepartment() != null ? dto.getAdminDepartment().trim() : "";
        if (!newDept.equals(admin.getDepartment() != null ? admin.getDepartment().trim() : "")) {
            newLog.put("adminDepartment", newDept);
            admin.setDepartment(newDept.isEmpty() ? null : newDept);
            isChanged = true;
        }

        // 4.7. Xử lý cập nhật Chức vụ
        String newPos = dto.getAdminPosition() != null ? dto.getAdminPosition().trim() : "";
        if (!newPos.equals(admin.getPosition() != null ? admin.getPosition().trim() : "")) {
            newLog.put("adminPosition", newPos);
            admin.setPosition(newPos.isEmpty() ? null : newPos);
            isChanged = true;
        }

        // 4.8. Xử lý cập nhật Số điện thoại cá nhân
        String newAdminPhone = dto.getAdminPhone() != null ? dto.getAdminPhone().trim() : "";
        if (!newAdminPhone.equals(admin.getPhone() != null ? admin.getPhone().trim() : "")) {
            newLog.put("adminPhone", newAdminPhone);
            admin.setPhone(newAdminPhone.isEmpty() ? null : newAdminPhone);
            isChanged = true;
        }

        // 5. Xử lý đổi mật khẩu
        if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) {
            if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại để xác thực việc đổi mật khẩu.");
            }
            if (!passwordEncoder.matches(dto.getCurrentPassword(), account.getPasswordHash())) {
                throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
            }
            if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
                throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
            }
            if (dto.getNewPassword().length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
            }

            account.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
            oldLog.put("password", "***");
            newLog.put("password", "Đã đổi mật khẩu mới");
            isChanged = true;
        }

        // 6. Lưu vào Database và Ghi Log
        if (isChanged) {
            hospital.setUpdatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            accountRepository.save(account);
            hospitalRepository.save(hospital);
            hospitalAdminRepository.save(admin);

            // Ghi Audit Trail
            try {
                String oldValueJson = objectMapper.writeValueAsString(oldLog);
                String newValueJson = objectMapper.writeValueAsString(newLog);
                saveAuditLog("UPDATE_ADMIN_PROFILE", hospital.getId(), oldValueJson, newValueJson, "Cập nhật thông tin hồ sơ bệnh viện (Tên: " + hospital.getFullName() + ")");
            } catch (Exception e) {
                System.err.println("Lỗi ghi log Audit Trail: " + e.getMessage());
            }
        }
    }

    private void saveAuditLog(String action, Integer targetRecordId, String oldValue, String newValue, String notes) {
        try {
            Integer adminId = 1;
            String adminEmail = "HOSPITAL_ADMIN";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof com.rpm.remotepatientmonitoring.config.CustomUserDetails) {
                Account acc = ((com.rpm.remotepatientmonitoring.config.CustomUserDetails) auth.getPrincipal()).getAccount();
                adminId = acc.getId();
                adminEmail = acc.getEmail();
            }

            String ipAddress = "Unknown";
            String deviceInfo = "Unknown";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    deviceInfo = request.getHeader("User-Agent");
                    if (deviceInfo != null && deviceInfo.length() > 250) deviceInfo = deviceInfo.substring(0, 250);
                    ipAddress = request.getHeader("X-Forwarded-For");
                    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) ipAddress = request.getRemoteAddr();
                    if (ipAddress != null && ipAddress.contains(",")) ipAddress = ipAddress.split(",")[0].trim();
                }
            } catch (Exception ignored) {}

            AuditTrail log = AuditTrail.builder()
                    .actorType("HOSPITAL_ADMIN")
                    .actorId(adminId)
                    .action(action)
                    .targetTable("hospitals")
                    .targetRecordId(targetRecordId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .notes(notes + " (Bởi: " + adminEmail + ")")
                    .build();

            auditTrailRepository.save(log);
        } catch (Exception e) {
            System.err.println("Cảnh báo: Không thể ghi Audit Log (" + action + ")");
        }
    }
}