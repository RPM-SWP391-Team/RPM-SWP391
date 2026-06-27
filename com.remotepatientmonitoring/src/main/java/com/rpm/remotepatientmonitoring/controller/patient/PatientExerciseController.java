package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientExercise;
import com.rpm.remotepatientmonitoring.repository.PatientExerciseRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/patient/api/exercise")
public class PatientExerciseController {

    @Autowired
    private PatientExerciseRepository patientExerciseRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Optional<Patient> opt = patientRepository.findByAccountId(userDetails.getAccount().getId());
                if (opt.isPresent()) {
                    return opt.get();
                }
            }
        }
        List<Patient> all = patientRepository.findAll();
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
        LocalDate today = LocalDate.now();

        PatientExercise exercise = new PatientExercise();
        exercise.setPatient(patient);
        exercise.setLogDate(today);
        exercise.setExerciseType(exerciseType);
        exercise.setDurationMinutes(durationMinutes);
        exercise.setStepsCount(stepsCount);

        PatientExercise saved = patientExerciseRepository.save(exercise);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ghi nhận bài tập thành công!");
        response.put("exerciseId", saved.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteExercise(@PathVariable("id") Integer id) {
        patientExerciseRepository.deleteById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa ghi nhận bài tập thành công!");
        return ResponseEntity.ok(response);
    }
}
