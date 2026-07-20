package com.rpm.remotepatientmonitoring.controller.hopital;

import com.rpm.remotepatientmonitoring.dto.hopital.HospitalOverviewDTO;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.Alert;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.hopital.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/hospital")
public class HospitalController {

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
    public String dashboard(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // 1. Mặc định lấy 7 ngày qua nếu không có tham số truyền vào
        if (startDate == null) startDate = LocalDate.now().minusDays(7);
        if (endDate == null) endDate = LocalDate.now();

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // 2. Lấy Overview từ Stored Procedure (giữ nguyên)
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

        // 3. Tính tỷ lệ tuân thủ thuốc
        long takenMeds = medicationLogRepository.countTakenMedicationLogs(HARDCODED_HOSPITAL_ID, startDate, endDate);
        long totalMeds = medicationLogRepository.countTotalMedicationLogs(HARDCODED_HOSPITAL_ID, startDate, endDate);
        double medicationCompliance = totalMeds > 0 ? ((double) takenMeds * 100.0 / totalMeds) : 0.0;
        model.addAttribute("medicationCompliance", medicationCompliance);
        model.addAttribute("takenMeds", takenMeds);
        model.addAttribute("totalMeds", totalMeds);

        // 4. Tính tỷ lệ nhập liệu đủ
        long loggedPatients = healthLogRepository.countPatientsWithLogsInRange(HARDCODED_HOSPITAL_ID, startDate, endDate);
        long totalTreating = patientRepository.countTotalTreatingPatients(HARDCODED_HOSPITAL_ID);
        double healthLogCompliance = totalTreating > 0 ? ((double) loggedPatients * 100.0 / totalTreating) : 0.0;
        model.addAttribute("healthLogCompliance", healthLogCompliance);
        model.addAttribute("loggedPatients", loggedPatients);
        model.addAttribute("totalTreating", totalTreating);

        // 5. Cảnh báo cấp độ 3 (Ép về 00:00:00 của startDate và 23:59:59 của endDate)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        long level3Alerts = alertRepository.countLevel3AlertsInRange(HARDCODED_HOSPITAL_ID, startDateTime, endDateTime);
        model.addAttribute("level3Alerts", level3Alerts);

        return "hospital/dashboard";
    }

    @GetMapping("/doctor/create")
    public String createDoctorPage() {
        return "redirect:/hospital/doctors";
    }

    @GetMapping("/admin/doctors/active")
    public String activeDoctors(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Doctor> doctorPage = doctorRepository.findByHospitalIdAndIsActiveTrue(HARDCODED_HOSPITAL_ID, pageable);

        model.addAttribute("title", "Danh sách Bác sĩ đang hoạt động");
        model.addAttribute("doctors", doctorPage.getContent()); // Vẫn trả ra List để Thymeleaf ko bị lỗi
        model.addAttribute("pageData", doctorPage); // Đẩy thêm data phân trang
        return "hospital/list_view";
    }

    @GetMapping("/admin/patients/treating")
    public String treatingPatients(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Patient> patientPage = patientRepository.findByHospitalIdAndStatus(HARDCODED_HOSPITAL_ID, "TREATING", pageable);

        model.addAttribute("title", "Danh sách Bệnh nhân đang điều trị");
        model.addAttribute("patients", patientPage.getContent());
        model.addAttribute("pageData", patientPage);
        return "hospital/list_view";
    }

    @GetMapping("/admin/patients/new")
    public String newPatients(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Patient> patientPage = patientRepository.findByHospitalIdAndStatus(HARDCODED_HOSPITAL_ID, "NEW", pageable);

        model.addAttribute("title", "Danh sách Bệnh nhân đăng ký mới");
        model.addAttribute("patients", patientPage.getContent());
        model.addAttribute("pageData", patientPage);
        return "hospital/list_view";
    }

    @GetMapping("/admin/alerts/red-pending")
    public String pendingRedAlerts(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Alert> alertPage = alertRepository.findUnresolvedRedAlertsByHospital(HARDCODED_HOSPITAL_ID, pageable);

        model.addAttribute("title", "Danh sách Cảnh báo Đỏ chưa giải quyết");
        model.addAttribute("alerts", alertPage.getContent());
        model.addAttribute("pageData", alertPage);
        return "hospital/list_view";
    }
}