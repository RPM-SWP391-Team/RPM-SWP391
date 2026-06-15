package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMeal;
import com.rpm.remotepatientmonitoring.repository.patient.PatientMealRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/patient/api/nutrition")
public class PatientNutritionController {

    @Autowired
    private PatientMealRepository patientMealRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Patient getCurrentPatient() {
        return patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống."));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMeal(
            @RequestParam("mealType") String mealType,
            @RequestParam("foodName") String foodName,
            @RequestParam("calories") Integer calories,
            @RequestParam("saltG") Double saltG,
            @RequestParam("fiberG") Double fiberG) {

        Patient patient = getCurrentPatient();
        LocalDate today = LocalDate.now();

        PatientMeal meal = PatientMeal.builder()
                .patient(patient)
                .logDate(today)
                .mealType(mealType)
                .foodName(foodName)
                .calories(calories)
                .saltG(saltG)
                .fiberG(fiberG)
                .build();

        PatientMeal saved = patientMealRepository.save(meal);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ghi nhận bữa ăn thành công!",
                "mealId", saved.getId()
        ));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteMeal(@PathVariable("id") Integer id) {
        patientMealRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa ghi nhận bữa ăn thành công!"
        ));
    }
}
