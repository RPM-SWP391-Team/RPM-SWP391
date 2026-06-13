package com.rpm.remotepatientmonitoring.controller.doctor;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.dto.doctor.AssignPatientRequestDTO;
import com.rpm.remotepatientmonitoring.dto.doctor.PatientSearchResponseDTO;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.service.doctor.DoctorPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorPatientService doctorPatientService;

    @Autowired
    private DoctorRepository doctorRepository;

    // API 1: Tìm kiếm bệnh nhân chờ tiếp nhận (chỉ trong cùng bệnh viện)
    // Ví dụ gọi: GET http://localhost:8080/api/doctor/patients/search?keyword=09
    @GetMapping("/patients/search")
    public ResponseEntity<List<PatientSearchResponseDTO>> searchPatients(
            @RequestParam String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Lấy doctor từ session để biết hospitalId
        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        List<PatientSearchResponseDTO> patients = doctorPatientService.searchUnassignedPatients(keyword, doctor.getHospital().getId());
        return ResponseEntity.ok(patients);
    }

    // API 2: Gán bệnh nhân vào bác sĩ — doctorId lấy từ session, không nhận từ client
    // Ví dụ gọi: POST http://localhost:8080/api/doctor/patients/assign
    @PostMapping("/patients/assign")
    public ResponseEntity<String> assignPatient(
            @RequestBody AssignPatientRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Lấy doctorId từ người dùng đang đăng nhập, không tin vào request body
        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        String message = doctorPatientService.assignPatient(
                request.getPatientId(),
                doctor.getId(),              // ← lấy từ session, không phải request
                request.getDiseaseProfileId()
        );
        return ResponseEntity.ok(message);
    }
}