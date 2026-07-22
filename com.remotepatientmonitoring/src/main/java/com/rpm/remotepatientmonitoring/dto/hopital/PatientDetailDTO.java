package com.rpm.remotepatientmonitoring.dto.hopital;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDetailDTO {
    private Integer id;
    private String patientCode;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String status;
    private String registrationSource;
    private String diseaseProfileName;
    private String doctorName;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
