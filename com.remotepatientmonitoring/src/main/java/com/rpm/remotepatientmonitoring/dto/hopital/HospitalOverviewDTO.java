package com.rpm.remotepatientmonitoring.dto.hopital;

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

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public Integer getTotalTreatingPatients() { return totalTreatingPatients; }
    public void setTotalTreatingPatients(Integer totalTreatingPatients) { this.totalTreatingPatients = totalTreatingPatients; }
    public Integer getTotalNewPatients() { return totalNewPatients; }
    public void setTotalNewPatients(Integer totalNewPatients) { this.totalNewPatients = totalNewPatients; }
    public Integer getRedUnresolvedAlerts() { return redUnresolvedAlerts; }
    public void setRedUnresolvedAlerts(Integer redUnresolvedAlerts) { this.redUnresolvedAlerts = redUnresolvedAlerts; }
    public Integer getActiveDoctorsCount() { return activeDoctorsCount; }
    public void setActiveDoctorsCount(Integer activeDoctorsCount) { this.activeDoctorsCount = activeDoctorsCount; }
}
