package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.model.OtpCode;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import com.rpm.remotepatientmonitoring.repository.OtpCodeRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private com.rpm.remotepatientmonitoring.service.doctor.AuditTrailService auditTrailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final SecureRandom secureRandom = new SecureRandom();

    public boolean emailExists(String email) {
        Optional<Account> opt = accountRepository.findByEmail(email);
        if (opt.isPresent()) {
            return opt.get().getIsEmailVerified();
        }
        return false;
    }

    public boolean phoneExists(String phone) {
        return patientRepository.findByPhone(phone).isPresent();
    }

    // ==================== REGISTER (CHỈ LÀM DB) ====================

    /**
     * Đăng ký bệnh nhân — CHỈ lưu Account + Patient + OtpCode vào DB.
     * KHÔNG gửi email trong transaction này.
     * Controller sẽ gọi sendOtpEmailSafely() SAU KHI method này return (transaction đã commit).
     *
     * @return mã OTP đã tạo (để controller truyền cho sendOtpEmailSafely)
     */
    @Transactional
    public String registerPatient(String email, String password, String fullName, String phone,
                                  String dateOfBirth, String gender, String address,
                                  String emergencyContactName, String emergencyContactPhone) {
        // 1. Tìm hoặc tạo Account
        Optional<Account> existingOpt = accountRepository.findByEmail(email);
        Account account;
        if (existingOpt.isPresent()) {
            account = existingOpt.get();
            // Nếu đã xác thực email, chặn lại (phòng hờ race condition)
            if (Boolean.TRUE.equals(account.getIsEmailVerified())) {
                throw new IllegalStateException("Email này đã được xác thực và sử dụng!");
            }
            account.setPasswordHash(passwordEncoder.encode(password));
            account.setUpdatedAt(LocalDateTime.now());
            log.info("Cập nhật lại Account id={} chưa xác thực cho email={}", account.getId(), email);
        } else {
            account = new Account();
            account.setEmail(email);
            account.setPasswordHash(passwordEncoder.encode(password));
            account.setRole("PATIENT");
            account.setIsEmailVerified(false);
            account.setIsActive(true);
        }

        // Serialize thông tin Patient và lưu vào registrationDetails
        try {
            Map<String, String> details = new HashMap<>();
            details.put("fullName", fullName);
            details.put("phone", phone);
            details.put("dateOfBirth", dateOfBirth);
            details.put("gender", gender);
            details.put("address", address);
            details.put("emergencyContactName", emergencyContactName);
            details.put("emergencyContactPhone", emergencyContactPhone);

            String detailsJson = objectMapper.writeValueAsString(details);
            account.setRegistrationDetails(detailsJson);
        } catch (Exception e) {
            log.error("Lỗi serialize registration details cho email={}: {}", email, e.getMessage());
            throw new RuntimeException("Lỗi xử lý thông tin đăng ký: " + e.getMessage());
        }

        Account savedAccount = accountRepository.save(account);
        log.info("Đã lưu Account id={} cho email={}", savedAccount.getId(), email);

        // 2. Tạo OTP record trong DB (KHÔNG gửi email ở đây)
        String otp = createOtpRecord(email, "REGISTRATION");
        log.info("Đã tạo OTP record cho email={}", email);

        return otp;
    }

    // ==================== OTP - CHỈ TẠO RECORD TRONG DB ====================

    /**
     * Tạo mã OTP 6 chữ số và lưu vào bảng otp_codes.
     * Vô hiệu hóa tất cả OTP cũ cùng email + otpType trước khi tạo mới.
     * KHÔNG gửi email — chỉ làm DB.
     *
     * @return mã OTP đã tạo
     */
    @Transactional
    public String createOtpRecord(String email, String otpType) {
        // Vô hiệu hóa tất cả OTP cũ chưa dùng
        List<OtpCode> oldOtps = otpCodeRepository.findByEmailAndOtpTypeAndIsUsedFalse(email, otpType);
        for (OtpCode old : oldOtps) {
            old.setIsUsed(true);
        }
        if (!oldOtps.isEmpty()) {
            otpCodeRepository.saveAll(oldOtps);
            log.info("Đã vô hiệu hóa {} OTP cũ cho email={}, type={}", oldOtps.size(), email, otpType);
        }

        // Tạo mã OTP mới
        String otp = generateOtpCode();

        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .otpCode(otp)
                .otpType(otpType)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpCodeRepository.save(otpCode);
        log.info("Đã tạo OTP mới cho email={}, type={}, expires={}",
                email, otpType, otpCode.getExpiresAt());

        return otp;
    }

    // ==================== GỬI EMAIL (NGOÀI TRANSACTION) ====================

    /**
     * Gửi email OTP một cách an toàn — KHÔNG nằm trong @Transactional.
     * Nếu gửi email fail, chỉ log lỗi, KHÔNG ảnh hưởng dữ liệu DB đã commit.
     * User có thể nhấn "Gửi lại OTP" nếu không nhận được email.
     */
    public void sendOtpEmailSafely(String email, String otp, String otpType) {
        try {
            emailService.sendOtpEmail(email, otp, otpType);
            log.info("Đã gửi OTP email thành công cho email={}", email);
        } catch (Exception e) {
            log.error("LỖI GỬI EMAIL OTP cho {}: {} — Root cause: {}",
                    email, e.getMessage(), getRootCause(e).getMessage(), e);
            // KHÔNG throw — dữ liệu DB đã an toàn, user dùng nút "Gửi lại OTP"
        }
    }

    // ==================== OTP VERIFICATION ====================

    /**
     * Xác thực mã OTP:
     * - Tìm OTP chưa dùng khớp email + otpCode + otpType
     * - Kiểm tra hết hạn
     * - Đánh dấu is_used = true
     * - Nếu otpType = REGISTER, set is_email_verified = true cho Account
     *
     * @return thông báo lỗi nếu không hợp lệ, null nếu thành công
     */
    @Transactional
    public String verifyOtp(String email, String otpInput, String otpType) {
        log.info("Xác thực OTP cho email={}, type={}", email, otpType);

        Optional<OtpCode> optOtp = otpCodeRepository
                .findByEmailAndOtpCodeAndOtpTypeAndIsUsedFalse(email, otpInput, otpType);

        if (optOtp.isEmpty()) {
            log.warn("OTP không tìm thấy hoặc đã sử dụng: email={}, type={}", email, otpType);
            return "Mã OTP không đúng hoặc đã được sử dụng!";
        }

        OtpCode otpCode = optOtp.get();

        // Kiểm tra hết hạn
        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpCode.setIsUsed(true);
            otpCodeRepository.save(otpCode);
            log.warn("OTP đã hết hạn: email={}, expiresAt={}", email, otpCode.getExpiresAt());
            return "Mã OTP đã hết hạn! Vui lòng yêu cầu gửi lại.";
        }

        // Đánh dấu đã sử dụng
        otpCode.setIsUsed(true);
        otpCodeRepository.save(otpCode);

        // Nếu là OTP đăng ký, cập nhật trạng thái xác thực email và tạo Patient record
        if ("REGISTRATION".equals(otpType)) {
            Optional<Account> optAccount = accountRepository.findByEmail(email);
            if (optAccount.isPresent()) {
                Account account = optAccount.get();
                account.setIsEmailVerified(true);

                // Tạo Patient từ registrationDetails
                String detailsJson = account.getRegistrationDetails();
                if (detailsJson != null && !detailsJson.trim().isEmpty()) {
                    try {
                        Map<String, String> details = objectMapper.readValue(detailsJson, Map.class);
                        String fullName = details.get("fullName");
                        String phone = details.get("phone");
                        String dateOfBirth = details.get("dateOfBirth");
                        String gender = details.get("gender");
                        String address = details.get("address");
                        String emergencyContactName = details.get("emergencyContactName");
                        String emergencyContactPhone = details.get("emergencyContactPhone");

                        Hospital hospital = hospitalRepository.findAll().stream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                        "Chưa có bệnh viện nào trong hệ thống! Vui lòng liên hệ quản trị viên."));

                        LocalDate dob = null;
                        if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                            try {
                                dob = LocalDate.parse(dateOfBirth);
                            } catch (Exception e) {
                                log.warn("Không thể parse ngày sinh '{}', bỏ qua: {}", dateOfBirth, e.getMessage());
                            }
                        }

                        Patient patient = Patient.builder()
                                .account(account)
                                .hospital(hospital)
                                .fullName(fullName)
                                .phone(phone)
                                .dateOfBirth(dob)
                                .gender(gender != null && !gender.trim().isEmpty() ? gender.trim() : null)
                                .address(address != null && !address.trim().isEmpty() ? address.trim() : null)
                                .emergencyContactName(emergencyContactName != null && !emergencyContactName.trim().isEmpty() ? emergencyContactName.trim() : null)
                                .emergencyContactPhone(emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty() ? emergencyContactPhone.trim() : null)
                                .status("NEW")
                                .registrationSource("ONLINE")
                                .isActive(true)
                                .build();
                        
                        patientRepository.save(patient);
                        log.info("Đã tạo Patient cho Account id={} sau khi xác thực OTP thành công", account.getId());

                        if (auditTrailService != null) {
                            try {
                                auditTrailService.logAction(
                                        "PATIENT",
                                        patient.getId(),
                                        "PATIENT_REGISTER",
                                        "patients",
                                        patient.getId(),
                                        null,
                                        patient,
                                        "Bệnh nhân " + patient.getFullName() + " xác thực OTP kích hoạt tài khoản thành công"
                                );
                            } catch (Exception ex) {
                                log.warn("Không thể lưu audit log cho patient registration: {}", ex.getMessage());
                            }
                        }
                        
                        // Clear registrationDetails để dọn dẹp DB
                        account.setRegistrationDetails(null);
                    } catch (Exception e) {
                        log.error("Lỗi khi tạo Patient từ registrationDetails cho email={}: {}", email, e.getMessage(), e);
                        throw new RuntimeException("Lỗi tạo thông tin bệnh nhân: " + e.getMessage());
                    }
                } else {
                    log.warn("Không tìm thấy registrationDetails cho Account email={}", email);
                }

                accountRepository.save(account);
                log.info("Đã xác thực email thành công cho Account id={}", account.getId());
            } else {
                log.warn("Không tìm thấy Account cho email={} khi verify OTP", email);
            }
        }

        return null; // Thành công
    }

    // ==================== RESEND OTP ====================

    /**
     * Tạo OTP mới trong DB (transactional).
     * Controller sẽ gọi sendOtpEmailSafely() sau khi method này return.
     *
     * @return mã OTP mới
     */
    @Transactional
    public String resendOtp(String email, String otpType) {
        return createOtpRecord(email, otpType);
    }

    // ==================== RESET PASSWORD ====================

    @Transactional
    public void resetPassword(String email, String newPassword) {
        log.info("Đặt lại mật khẩu cho email={}", email);
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent() == false) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản với email này!");
        }
        Account account = accountOpt.get();
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        log.info("Đặt lại mật khẩu thành công cho Account id={}", account.getId());
    }

    // ==================== HELPER ====================

    private String generateOtpCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", code);
    }

    /**
     * Tìm root cause thực sự của exception chain.
     */
    private Throwable getRootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}