package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Appointment;
import com.rpm.remotepatientmonitoring.model.ChangeRequest;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AppointmentRepository;
import com.rpm.remotepatientmonitoring.repository.ChangeRequestRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientInteractionController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ChangeRequestRepository changeRequestRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // =========================================================
    // Dashboard bệnh nhân (landing page sau khi đăng nhập)
    // =========================================================
    @GetMapping("/dashboard")
    public String patientDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Patient patient = patientRepository.findByAccountId(userDetails.getAccount().getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ bệnh nhân cho tài khoản này."));

        model.addAttribute("patient", patient);
        return "patient/dashboard";
    }

    // =========================================================
    // Danh sách lịch hẹn
    // =========================================================
    @GetMapping("/appointments")
    public String getAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Patient patient = patientRepository.findByAccountId(userDetails.getAccount().getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ bệnh nhân cho tài khoản này."));

        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patient.getId());
        List<ChangeRequest> changeRequests = changeRequestRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("changeRequests", changeRequests);
        return "patient/appointments";
    }

    // =========================================================
    // Trang yêu cầu thay đổi
    // =========================================================
    @GetMapping("/request-change")
    public String requestChangePage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Patient patient = patientRepository.findByAccountId(userDetails.getAccount().getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ bệnh nhân cho tài khoản này."));

        ChangeRequest changeRequest = ChangeRequest.builder()
                .status("PENDING")
                .build();

        model.addAttribute("patient", patient);
        model.addAttribute("changeRequest", changeRequest);
        return "patient/request-change";
    }

    @PostMapping("/request-change")
    public String submitChangeRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("changeRequest") ChangeRequest changeRequest) {

        Patient patient = patientRepository.findByAccountId(userDetails.getAccount().getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hồ sơ bệnh nhân cho tài khoản này."));

        Doctor doctor = patient.getDoctor();
        if (doctor == null) {
            doctor = doctorRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy bác sĩ nào trong hệ thống."));
        }

        changeRequest.setPatient(patient);
        changeRequest.setDoctor(doctor);
        changeRequest.setStatus("PENDING");
        changeRequest.setCreatedAt(LocalDateTime.now());
        changeRequest.setUpdatedAt(LocalDateTime.now());

        changeRequestRepository.save(changeRequest);
        return "redirect:/patient/appointments?requestSuccess=true";
    }
}

