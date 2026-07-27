package com.rpm.remotepatientmonitoring.controller.hospital;

import com.rpm.remotepatientmonitoring.dto.hospital.PatientDetailDTO;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hospital")
public class HospitalPatientController {

    @Autowired
    private PatientRepository patientRepository;

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @GetMapping("/patients")
    public String listPatients(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "assignStatus", defaultValue = "ALL") String assignStatus,
            @RequestParam(value = "diseaseCode", defaultValue = "ALL") String diseaseCode,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        
        // Tránh lỗi null hoặc rỗng cho tham số tìm kiếm
        String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
        
        Page<Patient> patientPage = patientRepository.findPatientsWithFilters(
                HARDCODED_HOSPITAL_ID, searchParam, assignStatus, diseaseCode, pageable);

        model.addAttribute("patientPage", patientPage);
        model.addAttribute("patients", patientPage.getContent());
        model.addAttribute("searchKeyword", search);
        model.addAttribute("selectedAssignStatus", assignStatus);
        model.addAttribute("selectedDiseaseCode", diseaseCode);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", patientPage.getTotalPages());
        model.addAttribute("totalItems", patientPage.getTotalElements());

        return "hospital/patients";
    }

    @GetMapping("/patients/{id}")
    @ResponseBody
    public ResponseEntity<PatientDetailDTO> getPatientDetail(@PathVariable Integer id) {
        java.util.Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent()) {
            Patient p = patientOpt.get();
            PatientDetailDTO dto = PatientDetailDTO.builder()
                    .id(p.getId())
                    .patientCode(p.getPatientCode())
                    .fullName(p.getFullName())
                    .email(p.getAccount() != null ? p.getAccount().getEmail() : null)
                    .phone(p.getPhone())
                    .gender(p.getGender() != null ? ("MALE".equalsIgnoreCase(p.getGender()) ? "Nam" : ("FEMALE".equalsIgnoreCase(p.getGender()) ? "Nữ" : "Khác")) : "-")
                    .dateOfBirth(p.getDateOfBirth())
                    .address(p.getAddress())
                    .status("NEW".equals(p.getStatus()) ? "Mới đăng ký" : ("TREATING".equals(p.getStatus()) ? "Đang điều trị" : p.getStatus()))
                    .registrationSource(p.getRegistrationSource())
                    .diseaseProfileName(p.getDiseaseProfile() != null ? p.getDiseaseProfile().getProfileName() : "Không có")
                    .doctorName(p.getDoctor() != null ? p.getDoctor().getFullName() : "Chưa phân công")
                    .emergencyContactName(p.getEmergencyContactName() != null ? p.getEmergencyContactName() : "-")
                    .emergencyContactPhone(p.getEmergencyContactPhone() != null ? p.getEmergencyContactPhone() : "-")
                    .isActive(p.getIsActive())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build();
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
