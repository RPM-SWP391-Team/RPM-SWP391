package com.rpm.remotepatientmonitoring.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDetailDTO {
    private String medicineName;
    private String dosage;
    private String scheduledTime;
    private Boolean isTaken;
    private LocalDateTime takenAt;
}
