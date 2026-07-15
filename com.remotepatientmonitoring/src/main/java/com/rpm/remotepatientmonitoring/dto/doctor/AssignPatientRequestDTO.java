package com.rpm.remotepatientmonitoring.dto.doctor;

import lombok.Data;

@Data
public class AssignPatientRequestDTO {
    private Integer patientId;
    private Integer diseaseProfileId;
}