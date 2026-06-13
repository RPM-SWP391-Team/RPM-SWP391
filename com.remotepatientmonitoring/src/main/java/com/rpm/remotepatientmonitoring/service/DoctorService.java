package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Doctor> getDoctorsByHospital(Integer hospitalId) {
        return doctorRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public Doctor createDoctor(Integer hospitalId, String doctorCode, String fullName, String phone, String email, String password, String specialty, Integer capacityLimit) {
        // 1. Data validation
        if (doctorCode == null || doctorCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã bác sĩ không được để trống.");
        }
        if (!doctorCode.matches("^[a-zA-Z0-9_-]{2,50}$")) {
            throw new IllegalArgumentException("Mã bác sĩ chỉ được chứa chữ cái, số, dấu gạch ngang hoặc gạch dưới (độ dài 2-50).");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        if (!phone.matches("^[0-9]{9,15}$")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (chỉ được nhập số, độ dài từ 9 đến 15 ký tự).");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            throw new IllegalArgumentException("Địa chỉ email không đúng định dạng.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải chứa ít nhất 6 ký tự.");
        }
        if (capacityLimit == null || capacityLimit < 1 || capacityLimit > 500) {
            throw new IllegalArgumentException("Giới hạn số bệnh nhân (Capacity Limit) phải nằm trong khoảng từ 1 đến 500.");
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

        // 2. Create Account
        Account account = Account.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role("DOCTOR")
                .isEmailVerified(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        // 3. Create Doctor
        Doctor doctor = Doctor.builder()
                .account(account)
                .hospital(hospital)
                .doctorCode(doctorCode)
                .fullName(fullName)
                .phone(phone)
                .specialty(specialty)
                .capacityLimit(capacityLimit)
                .currentPatientCount(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deactivateDoctor(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + doctorId));
        
        doctor.setIsActive(false);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        // Also deactivate the login account
        if (doctor.getAccount() != null) {
            Account account = doctor.getAccount();
            account.setIsActive(false);
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
        }
    }
}
