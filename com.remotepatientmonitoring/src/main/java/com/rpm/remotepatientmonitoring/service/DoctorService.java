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

import java.security.SecureRandom;

@Service("generalDoctorService")
public class DoctorService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private String generatePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public Doctor createDoctor(String email, String fullName, String doctorCode,
                               String phone, String specialty, Integer hospitalId) {
        // Generate password
        String rawPassword = generatePassword();

        // Tạo account
        Account account = new Account();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(rawPassword));
        account.setRole("DOCTOR");
        account.setIsEmailVerified(true);
        account.setIsActive(true);
        accountRepository.save(account);

        // Tạo doctor
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        Doctor doctor = new Doctor();
        doctor.setAccount(account);
        doctor.setHospital(hospital);
        doctor.setFullName(fullName);
        doctor.setDoctorCode(doctorCode);
        doctor.setPhone(phone);
        doctor.setSpecialty(specialty);
        doctorRepository.save(doctor);

        // Gửi email
        emailService.sendDoctorPassword(email, fullName, rawPassword);

        return doctor;
    }
}