package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ⚠️ CONTROLLER TẠM THỜI — XÓA SAU KHI SETUP XONG
 * Không cần đăng nhập, dùng để:
 *  - Xem accounts, hospitals trong DB
 *  - Reset mật khẩu tài khoản bất kỳ bằng BCrypt thực
 *  - Tạo tài khoản bác sĩ mới
 */
@RestController
@RequestMapping("/setup")
public class SetupController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Xem tất cả accounts: GET http://localhost:8080/setup/accounts
     */
    @GetMapping("/accounts")
    public List<Map<String, Object>> listAccounts() {
        return accountRepository.findAll().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("email", a.getEmail());
            m.put("role", a.getRole());
            m.put("is_active", a.getIsActive());
            m.put("is_email_verified", a.getIsEmailVerified());
            m.put("hash_prefix", a.getPasswordHash() != null
                    ? a.getPasswordHash().substring(0, Math.min(10, a.getPasswordHash().length())) + "..."
                    : "null");
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * Xem bệnh viện: GET http://localhost:8080/setup/hospitals
     */
    @GetMapping("/hospitals")
    public List<Map<String, Object>> listHospitals() {
        return hospitalRepository.findAll().stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("fullName", h.getFullName());   // ← dùng getFullName() không phải getName()
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * Sinh BCrypt hash cho bất kỳ mật khẩu nào:
     * GET http://localhost:8080/setup/hash?password=abc123
     * → Dùng hash này để UPDATE trực tiếp trong SSMS
     */
    @GetMapping("/hash")
    public Map<String, String> generateHash(@RequestParam String password) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("password", password);
        result.put("bcrypt_hash", passwordEncoder.encode(password));
        result.put("usage", "Dùng hash này để UPDATE vào cột password_hash trong bảng accounts");
        return result;
    }

    /**
     * Reset mật khẩu tài khoản bất kỳ theo email:
     * GET http://localhost:8080/setup/reset?email=bacsi.an@rpm.com&password=Doctor@123
     */
    @GetMapping("/reset")
    public Map<String, Object> resetPassword(
            @RequestParam String email,
            @RequestParam(defaultValue = "Doctor@123") String password) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<Account> opt = accountRepository.findByEmail(email);
        if (opt.isEmpty()) {
            result.put("error", "Không tìm thấy account với email: " + email);
            return result;
        }
        Account account = opt.get();
        String newHash = passwordEncoder.encode(password);
        account.setPasswordHash(newHash);
        account.setIsActive(true);
        account.setIsEmailVerified(true);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        result.put("success", true);
        result.put("email", email);
        result.put("new_password", password);
        result.put("hash_generated", newHash);
        result.put("message", "Mật khẩu đã được reset. Hãy đăng nhập ngay!");
        return result;
    }

    /**
     * Tạo tài khoản bác sĩ mới (doctor@rpm.com / Doctor@123):
     * GET http://localhost:8080/setup/create-doctor
     */
    @GetMapping("/create-doctor")
    public Map<String, Object> createDoctor() {
        Map<String, Object> result = new LinkedHashMap<>();
        String email = "doctor@rpm.com";
        String rawPassword = "Doctor@123";

        // Nếu đã có → chỉ reset mật khẩu
        if (accountRepository.findByEmail(email).isPresent()) {
            return resetPassword(email, rawPassword);
        }

        // Lấy hospital đầu tiên
        List<Hospital> hospitals = hospitalRepository.findAll();
        if (hospitals.isEmpty()) {
            result.put("error", "Chưa có bệnh viện trong DB. Kiểm tra /setup/hospitals");
            return result;
        }
        Hospital hospital = hospitals.get(0);

        // Tạo Account
        Account account = new Account();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole("DOCTOR");
        account.setIsEmailVerified(true);
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        // Tạo Doctor
        Doctor doctor = new Doctor();
        doctor.setAccount(account);
        doctor.setHospital(hospital);
        doctor.setDoctorCode("BS-DEMO");
        doctor.setFullName("Bác Sĩ Demo");
        doctor.setPhone("0900000099");
        doctor.setSpecialty("Nội tổng quát");
        doctor.setCapacityLimit(50);
        doctor.setCurrentPatientCount(0);
        doctor.setIsActive(true);
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        result.put("success", true);
        result.put("email", email);
        result.put("password", rawPassword);
        result.put("hospital", hospital.getFullName());   // ← getFullName() đúng
        result.put("message", "Tạo tài khoản thành công! Đăng nhập tại /auth/login");
        return result;
    }
}
