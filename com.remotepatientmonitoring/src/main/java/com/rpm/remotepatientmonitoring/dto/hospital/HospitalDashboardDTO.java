package com.rpm.remotepatientmonitoring.dto.hospital;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalDashboardDTO {
    private HospitalOverviewDTO overview;
    private double medicationCompliance;
    private long takenMeds;
    private long totalMeds;
    private double healthLogCompliance;
    private long loggedPatients;
    private long totalTreating;
    private long level3Alerts;
}
