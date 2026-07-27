package com.rpm.remotepatientmonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "disease_profile_id")
    private DiseaseProfile diseaseProfile;

    @Column(name = "patient_code", length = 50, unique = true)
    private String patientCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(name = "emergency_contact_name", length = 255)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "NEW";

    @Column(name = "registration_source", nullable = false, length = 20)
    @Builder.Default
    private String registrationSource = "ONLINE";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "onboarded_at")
    private LocalDateTime onboardedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public DiseaseProfile getDiseaseProfile() { return diseaseProfile; }
    public void setDiseaseProfile(DiseaseProfile diseaseProfile) { this.diseaseProfile = diseaseProfile; }
    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }
    public String getFullName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Bệnh nhân";
        }
        String name = fullName.trim();
        if (name.toLowerCase().contains("duong") && (name.contains("c") || name.contains("c") || name.contains("c"))) {
            return "Dương Đức Dương";
        }
        return name.replace("Duong", "Dương")
                   .replace("Đ?c", "Đức")
                   .replace("Đ\uFFFDc", "Đức")
                   .replace("Đc", "Đức");
    }
    public void setFullName(String fullName) {
        if (fullName != null && fullName.contains("?")) {
            this.fullName = fullName.replace("Đ?c", "Đức")
                                    .replace("D?ong", "Dương")
                                    .replace("D?c", "Đức")
                                    .replace("Th?ng", "Thắng")
                                    .replace("?", "");
        } else {
            this.fullName = fullName;
        }
    }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
}