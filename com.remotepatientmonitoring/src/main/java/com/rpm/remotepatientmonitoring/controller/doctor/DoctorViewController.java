package com.rpm.remotepatientmonitoring.controller.doctor;

import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/doctor")
public class DoctorViewController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    // 1. Xử lý toàn bộ: Hiển thị, Tìm kiếm, và Phân trang trên Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {

        Doctor doctor = doctorRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        // Tạo đối tượng Pageable (trang hiện tại và số lượng 5 người/trang)
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientPage;

        // Nếu có nhập từ khóa -> Gọi hàm search có phân trang
        if (keyword != null && !keyword.trim().isEmpty()) {
            patientPage = patientRepository.searchPatientsForDoctor(1, keyword.trim(), pageable);
        } else {
            // Nếu không nhập gì -> Lấy tất cả bệnh nhân của bác sĩ này (có phân trang)
            patientPage = patientRepository.findByDoctorId(1, pageable);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("patientPage", patientPage);
        model.addAttribute("keyword", keyword);

        return "doctor/doctor-dashboard";
    }

    // 2. Mở trang Tìm kiếm & Tiếp nhận bệnh nhân mới
    @GetMapping("/assign")
    public String showAssignPage(Model model) {
        Doctor doctor = doctorRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        model.addAttribute("doctor", doctor);

        return "doctor/assign-patient";
    }
}