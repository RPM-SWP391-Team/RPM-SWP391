package com.rpm.remotepatientmonitoring.dto.doctor;

import lombok.Data;

@Data
public class PatientSearchResponseDTO {
    private Integer id;
    private String fullName;
    private String phone;
    private String status;
}