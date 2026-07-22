package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.PatientMeal;
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
@RequestMapping("/patient/api/nutrition")
public class PatientNutritionController {

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
    public ResponseEntity<Map<String, Object>> addMeal(
            @RequestParam("mealType") String mealType,
            @RequestParam("foodId") Integer foodId,
            @RequestParam("quantityG") Double quantityG) {

        if (mealType == null || mealType.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Loại bữa ăn không được để trống."
            ));
        }
        if (quantityG == null || quantityG <= 0.0 || quantityG > 10000.0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Khối lượng thực phẩm không hợp lệ (từ 1g đến 10000g)."
            ));
        }

        Patient patient = getCurrentPatient();

        try {
            PatientMeal saved = patientService.addMeal(patient, mealType, foodId, quantityG);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ghi nhận bữa ăn thành công!");
            response.put("mealId", saved.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteMeal(@PathVariable("id") Integer id) {
        patientService.deleteMeal(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa ghi nhận bữa ăn thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateMeal(
            @PathVariable("id") Integer id,
            @RequestParam("quantityG") Double quantityG) {
        if (quantityG == null || quantityG <= 0.0 || quantityG > 10000.0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Khối lượng thực phẩm không hợp lệ (từ 1g đến 10000g)."
            ));
        }
        try {
            patientService.updateMeal(id, quantityG);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cập nhật ghi nhận bữa ăn thành công!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
