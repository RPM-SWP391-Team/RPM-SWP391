package com.rpm.remotepatientmonitoring.service.hospital;

import com.rpm.remotepatientmonitoring.dto.hospital.HospitalDashboardDTO;
import com.rpm.remotepatientmonitoring.dto.hospital.HospitalOverviewDTO;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HospitalDashboardService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AlertRepository alertRepository;

    public HospitalDashboardDTO getDashboardData(Integer hospitalId, LocalDate startDate, LocalDate endDate) {
        // 1. Lấy Overview từ Stored Procedure
        List<Object[]> rawOverview = hospitalRepository.getHospitalOverviewRaw(hospitalId);
        HospitalOverviewDTO overview = new HospitalOverviewDTO();

        if (rawOverview != null && !rawOverview.isEmpty()) {
            Object[] row = rawOverview.get(0);
            overview.setHospitalName(row[0] != null ? row[0].toString() : "Bệnh viện");
            overview.setTotalTreatingPatients(row[1] != null ? ((Number) row[1]).intValue() : 0);
            overview.setTotalNewPatients(row[2] != null ? ((Number) row[2]).intValue() : 0);
            overview.setRedUnresolvedAlerts(row[3] != null ? ((Number) row[3]).intValue() : 0);
            overview.setActiveDoctorsCount(row[4] != null ? ((Number) row[4]).intValue() : 0);
        } else {
            overview.setHospitalName("Bệnh viện mẫu");
            overview.setTotalTreatingPatients(0);
            overview.setTotalNewPatients(0);
            overview.setRedUnresolvedAlerts(0);
            overview.setActiveDoctorsCount(0);
        }

        // 2. Tính tỷ lệ tuân thủ thuốc
        long takenMeds = medicationLogRepository.countTakenMedicationLogs(hospitalId, startDate, endDate);
        long totalMeds = medicationLogRepository.countTotalMedicationLogs(hospitalId, startDate, endDate);
        double medicationCompliance = totalMeds > 0 ? ((double) takenMeds * 100.0 / totalMeds) : 0.0;

        // 3. Tính tỷ lệ nhập liệu đủ
        long loggedPatients = healthLogRepository.countPatientsWithLogsInRange(hospitalId, startDate, endDate);
        long totalTreating = patientRepository.countTotalTreatingPatients(hospitalId);
        double healthLogCompliance = totalTreating > 0 ? ((double) loggedPatients * 100.0 / totalTreating) : 0.0;

        // 4. Cảnh báo cấp độ 3 (Ép về 00:00:00 của startDate và 23:59:59 của endDate)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        long level3Alerts = alertRepository.countLevel3AlertsInRange(hospitalId, startDateTime, endDateTime);

        return HospitalDashboardDTO.builder()
                .overview(overview)
                .medicationCompliance(medicationCompliance)
                .takenMeds(takenMeds)
                .totalMeds(totalMeds)
                .healthLogCompliance(healthLogCompliance)
                .loggedPatients(loggedPatients)
                .totalTreating(totalTreating)
                .level3Alerts(level3Alerts)
                .build();
    }
}
