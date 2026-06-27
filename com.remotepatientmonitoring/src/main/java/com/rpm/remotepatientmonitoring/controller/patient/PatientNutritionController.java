package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMeal;
import com.rpm.remotepatientmonitoring.model.FoodDictionary;
import com.rpm.remotepatientmonitoring.repository.PatientMealRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.FoodDictionaryRepository;
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
@RequestMapping("/patient/api/nutrition")
public class PatientNutritionController {

    @Autowired
    private PatientMealRepository patientMealRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FoodDictionaryRepository foodDictionaryRepository;

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
    public ResponseEntity<Map<String, Object>> addMeal(
            @RequestParam("mealType") String mealType,
            @RequestParam("foodId") Integer foodId,
            @RequestParam("quantityG") Double quantityG) {

        Patient patient = getCurrentPatient();
        LocalDate today = LocalDate.now();

        Optional<FoodDictionary> foodOpt = foodDictionaryRepository.findById(foodId);
        if (foodOpt.isPresent() == false) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không tìm thấy món ăn trong từ điển.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        FoodDictionary food = foodOpt.get();

        PatientMeal meal = new PatientMeal();
        meal.setPatient(patient);
        meal.setLogDate(today);
        meal.setMealType(mealType);
        meal.setFood(food);
        meal.setQuantityG(quantityG);

        PatientMeal saved = patientMealRepository.save(meal);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ghi nhận bữa ăn thành công!");
        response.put("mealId", saved.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteMeal(@PathVariable("id") Integer id) {
        patientMealRepository.deleteById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa ghi nhận bữa ăn thành công!");
        return ResponseEntity.ok(response);
    }
}
