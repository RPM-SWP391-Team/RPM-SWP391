package com.rpm.remotepatientmonitoring.controller.hospital;

import com.rpm.remotepatientmonitoring.dto.hospital.HospitalDashboardDTO;
import com.rpm.remotepatientmonitoring.dto.hospital.HospitalOverviewDTO;
import com.rpm.remotepatientmonitoring.service.hospital.HospitalDashboardService;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.Alert;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.hospital.DoctorService;
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
    private PatientRepository patientRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private HospitalDashboardService hospitalDashboardService;

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

        // Gọi Service lấy dữ liệu tổng hợp
        HospitalDashboardDTO dashboardData = hospitalDashboardService.getDashboardData(HARDCODED_HOSPITAL_ID, startDate, endDate);

        // Đẩy từng thuộc tính vào Model để đảm bảo giao diện Thymeleaf (dashboard.html) hoạt động hoàn hảo không bị lỗi
        model.addAttribute("overview", dashboardData.getOverview());
        model.addAttribute("medicationCompliance", dashboardData.getMedicationCompliance());
        model.addAttribute("takenMeds", dashboardData.getTakenMeds());
        model.addAttribute("totalMeds", dashboardData.getTotalMeds());
        model.addAttribute("healthLogCompliance", dashboardData.getHealthLogCompliance());
        model.addAttribute("loggedPatients", dashboardData.getLoggedPatients());
        model.addAttribute("totalTreating", dashboardData.getTotalTreating());
        model.addAttribute("level3Alerts", dashboardData.getLevel3Alerts());

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