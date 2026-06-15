package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientExercise;
import com.rpm.remotepatientmonitoring.repository.patient.PatientExerciseRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/patient/api/exercise")
public class PatientExerciseController {

    @Autowired
    private PatientExerciseRepository patientExerciseRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Patient getCurrentPatient() {
        return patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống."));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addExercise(
            @RequestParam("exerciseType") String exerciseType,
            @RequestParam("durationMinutes") Integer durationMinutes,
            @RequestParam("caloriesBurned") Integer caloriesBurned,
            @RequestParam(value = "notes", required = false) String notes) {

        Patient patient = getCurrentPatient();
        LocalDate today = LocalDate.now();

        PatientExercise exercise = PatientExercise.builder()
                .patient(patient)
                .logDate(today)
                .exerciseType(exerciseType)
                .durationMinutes(durationMinutes)
                .caloriesBurned(caloriesBurned != null ? caloriesBurned : 0)
                .notes(notes)
                .build();

        PatientExercise saved = patientExerciseRepository.save(exercise);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ghi nhận bài tập thành công!",
                "exerciseId", saved.getId()
        ));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteExercise(@PathVariable("id") Integer id) {
        patientExerciseRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa ghi nhận bài tập thành công!"
        ));
    }
}
