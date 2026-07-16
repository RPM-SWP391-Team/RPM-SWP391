package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientExercise;
import com.rpm.remotepatientmonitoring.service.patient.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/patient/api/exercise")
public class PatientExerciseController {

    @Autowired
    private PatientService patientService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Optional<Patient> opt = patientService.findByAccountId(userDetails.getAccount().getId());
                if (opt.isPresent()) {
                    return opt.get();
                }
            }
        }
        List<Patient> all = patientService.findAllPatients();
        if (all.size() > 0) {
            return all.get(0);
        }
        throw new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống.");
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addExercise(
            @RequestParam("exerciseType") String exerciseType,
            @RequestParam("durationMinutes") Integer durationMinutes,
            @RequestParam(value = "stepsCount", required = false) Integer stepsCount) {

        Patient patient = getCurrentPatient();
        PatientExercise saved = patientService.addPatientExercise(patient, exerciseType, durationMinutes, stepsCount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ghi nhận bài tập thành công!");
        response.put("exerciseId", saved.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteExercise(@PathVariable("id") Integer id) {
        patientService.deletePatientExercise(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa ghi nhận bài tập thành công!");
        return ResponseEntity.ok(response);
    }
}
