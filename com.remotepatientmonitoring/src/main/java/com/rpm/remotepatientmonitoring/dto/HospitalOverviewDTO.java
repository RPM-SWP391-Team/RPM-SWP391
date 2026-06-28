package com.rpm.remotepatientmonitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalOverviewDTO {
    private String hospitalName;
    private Integer totalTreatingPatients;
    private Integer totalNewPatients;
    private Integer redUnresolvedAlerts;
    private Integer activeDoctorsCount;
}
