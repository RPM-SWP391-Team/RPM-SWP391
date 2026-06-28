package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.dto.HospitalOverviewDTO;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.Alert;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/hospital")
public class HospitalController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Lấy thông tin tổng quan từ Stored Procedure thông qua HospitalRepository
        List<Object[]> rawOverview = hospitalRepository.getHospitalOverviewRaw(HARDCODED_HOSPITAL_ID);
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
        model.addAttribute("overview", overview);

        // 2. Tính toán tỷ lệ tuân thủ thuốc (%) trong tuần qua
        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);
        long takenMeds = medicationLogRepository.countTakenMedicationLogs(HARDCODED_HOSPITAL_ID, oneWeekAgo);
        long totalMeds = medicationLogRepository.countTotalMedicationLogs(HARDCODED_HOSPITAL_ID, oneWeekAgo);
        double medicationCompliance = totalMeds > 0 ? ((double) takenMeds * 100.0 / totalMeds) : 0.0;
        model.addAttribute("medicationCompliance", medicationCompliance);
        model.addAttribute("takenMeds", takenMeds);
        model.addAttribute("totalMeds", totalMeds);

        // 3. Tính toán tỷ lệ nhập liệu đủ (%) hôm nay
        LocalDate today = LocalDate.now();
        long loggedPatients = healthLogRepository.countPatientsWithLogsToday(HARDCODED_HOSPITAL_ID, today);
        long totalTreating = patientRepository.countTotalTreatingPatients(HARDCODED_HOSPITAL_ID);
        double healthLogCompliance = totalTreating > 0 ? ((double) loggedPatients * 100.0 / totalTreating) : 0.0;
        model.addAttribute("healthLogCompliance", healthLogCompliance);
        model.addAttribute("loggedPatients", loggedPatients);
        model.addAttribute("totalTreating", totalTreating);

        // 4. Số ca cảnh báo Level 3 trong tháng hiện tại
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long level3Alerts = alertRepository.countLevel3AlertsInMonth(HARDCODED_HOSPITAL_ID, startOfMonth);
        model.addAttribute("level3Alerts", level3Alerts);

        return "hospital/dashboard";
    }

    @GetMapping("/doctor/create")
    public String createDoctorPage() {
        return "redirect:/hospital/doctors";
    }

    @GetMapping("/admin/doctors/active")
    public String activeDoctors(Model model) {
        List<Doctor> doctors = doctorRepository.findByHospitalIdAndIsActiveTrue(HARDCODED_HOSPITAL_ID);
        model.addAttribute("title", "Danh sách Bác sĩ đang hoạt động");
        model.addAttribute("doctors", doctors);
        return "hospital/list_view";
    }

    @GetMapping("/admin/patients/treating")
    public String treatingPatients(Model model) {
        List<Patient> patients = patientRepository.findByHospitalIdAndStatus(HARDCODED_HOSPITAL_ID, "TREATING");
        model.addAttribute("title", "Danh sách Bệnh nhân đang điều trị");
        model.addAttribute("patients", patients);
        return "hospital/list_view";
    }

    @GetMapping("/admin/patients/new")
    public String newPatients(Model model) {
        List<Patient> patients = patientRepository.findByHospitalIdAndStatus(HARDCODED_HOSPITAL_ID, "NEW");
        model.addAttribute("title", "Danh sách Bệnh nhân đăng ký mới");
        model.addAttribute("patients", patients);
        return "hospital/list_view";
    }

    @GetMapping("/admin/alerts/red-pending")
    public String pendingRedAlerts(Model model) {
        List<Alert> alerts = alertRepository.findUnresolvedRedAlertsByHospital(HARDCODED_HOSPITAL_ID);
        model.addAttribute("title", "Danh sách Cảnh báo Đỏ chưa giải quyết");
        model.addAttribute("alerts", alerts);
        return "hospital/list_view";
    }
}