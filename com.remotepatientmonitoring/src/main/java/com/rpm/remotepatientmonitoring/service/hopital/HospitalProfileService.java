package com.rpm.remotepatientmonitoring.service.hopital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.dto.hopital.HospitalProfileDTO;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.AuditTrail;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.AuditTrailRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
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

    public HospitalProfileDTO getProfileByHospitalId(Integer hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin bệnh viện."));

        return HospitalProfileDTO.builder()
                .accountId(hospital.getAccount() != null ? hospital.getAccount().getId() : null)
                .email(hospital.getAccount() != null ? hospital.getAccount().getEmail() : "")
                .hospitalCode(hospital.getHospitalCode())
                .fullName(hospital.getFullName())
                .address(hospital.getAddress())
                .phone(hospital.getPhone())
                .build();
    }

    @Transactional
    public void updateProfile(Integer hospitalId, HospitalProfileDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin bệnh viện."));

        Account account = hospital.getAccount();

        // -------------------------------------------------------------
        // TẠO SNAPSHOT (BẢN SAO CHỤP) DỮ LIỆU ĐỂ GHI LOG GIỐNG BÁC SĨ
        // -------------------------------------------------------------
        Map<String, Object> oldLog = new HashMap<>();
        oldLog.put("email", account.getEmail());
        oldLog.put("fullName", hospital.getFullName());
        oldLog.put("phone", hospital.getPhone());
        oldLog.put("address", hospital.getAddress());

        // newLog lấy nguyên bản từ oldLog, sau đó trường nào thay đổi thì put đè lên
        Map<String, Object> newLog = new HashMap<>(oldLog);
        boolean isChanged = false;

        // 1. Xử lý cập nhật Email
        String newEmail = dto.getEmail().trim();
        if (!account.getEmail().equalsIgnoreCase(newEmail)) {
            if (accountRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email này đã được sử dụng bởi tài khoản khác.");
            }
            newLog.put("email", newEmail); // Ghi đè giá trị mới
            account.setEmail(newEmail);
            isChanged = true;
        }

        // 2. Xử lý đổi mật khẩu
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
            // Ghi log bảo mật ẩn Password
            oldLog.put("password", "***");
            newLog.put("password", "Đã đổi mật khẩu mới");
            isChanged = true;
        }

        // 3. Xử lý thông tin Bệnh viện (Kiểm tra null trước khi trim)
        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty() && !Objects.equals(hospital.getFullName(), dto.getFullName().trim())) {
            newLog.put("fullName", dto.getFullName().trim());
            hospital.setFullName(dto.getFullName().trim());
            isChanged = true;
        }

        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty() && !Objects.equals(hospital.getPhone(), dto.getPhone().trim())) {
            newLog.put("phone", dto.getPhone().trim());
            hospital.setPhone(dto.getPhone().trim());
            isChanged = true;
        }

        if (dto.getAddress() != null && !dto.getAddress().trim().isEmpty() && !Objects.equals(hospital.getAddress(), dto.getAddress().trim())) {
            newLog.put("address", dto.getAddress().trim());
            hospital.setAddress(dto.getAddress().trim());
            isChanged = true;
        }

        // 4. Lưu vào Database và Ghi Log
        if (isChanged) {
            hospital.setUpdatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());

            accountRepository.save(account);
            hospitalRepository.save(hospital);

            // Ghi Audit Trail
            try {
                String oldValueJson = objectMapper.writeValueAsString(oldLog);
                String newValueJson = objectMapper.writeValueAsString(newLog);
                saveAuditLog("UPDATE_HOSPITAL_PROFILE", hospital.getId(), oldValueJson, newValueJson, "Cập nhật hồ sơ bệnh viện & tài khoản quản trị");
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