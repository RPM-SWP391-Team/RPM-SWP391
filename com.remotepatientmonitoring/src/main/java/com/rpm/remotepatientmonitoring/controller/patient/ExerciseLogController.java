package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.ExerciseLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.service.patient.ExerciseLogService;
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patient")
public class ExerciseLogController {

    @Autowired
    private ExerciseLogService exerciseLogService;

    @Autowired
    private PatientHealthService patientHealthService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Patient patient = patientHealthService.getPatientByAccountId(userDetails.getAccount().getId());
                if (patient != null) {
                    return patient;
                }
            }
        }
        List<Patient> all = patientHealthService.getAllPatients();
        if (all.size() > 0) {
            return all.get(0);
        }
        throw new IllegalStateException("Không tìm thấy bệnh nhân trong hệ thống.");
    }

    @GetMapping("/exercise")
    public String getExercisePage(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        if ("NEW".equals(patient.getStatus())) {
            return "redirect:/patient/appointments";
        }

        Map<String, Object> summary = exerciseLogService.getTodaySummary(patient.getId());
        List<ExerciseLog> logs = exerciseLogService.getTodayLogs(patient.getId());
        List<Map<String, Object>> history7 = exerciseLogService.getHistorySummary(patient.getId(), 7);
        List<Map<String, Object>> history30 = exerciseLogService.getHistorySummary(patient.getId(), 30);
        int streak = exerciseLogService.getCurrentStreak(patient.getId());

        // Lấy chỉ số BMI gần nhất
        Map<String, Object> latestBmi = exerciseLogService.getLatestBmi(patient.getId());

        // Lấy danh sách thông báo tập luyện chưa đọc hôm nay
        List<com.rpm.remotepatientmonitoring.model.Notification> exerciseNotifications = 
                exerciseLogService.getUnreadExerciseNotificationsToday(patient.getId());

        model.addAttribute("patient", patient);
        model.addAttribute("exercises", logs);
        model.addAttribute("totalMinutes", summary.get("totalMinutes"));
        model.addAttribute("totalCaloriesBurned", summary.get("totalCaloriesBurned"));
        model.addAttribute("targetMinutes", summary.get("targetMinutes"));
        model.addAttribute("exerciseProgress", summary.get("exerciseProgress"));
        model.addAttribute("history7Days", history7);
        model.addAttribute("history30Days", history30);
        model.addAttribute("streak", streak);
        model.addAttribute("warningThreshold", ExerciseLogService.HIGH_INTENSITY_WARNING_THRESHOLD);
        model.addAttribute("latestBmi", latestBmi);
        model.addAttribute("exerciseNotifications", exerciseNotifications);

        return "patient/exercise";
    }

    @PostMapping("/exercise")
    @ResponseBody
    public Object saveExercise(
            @RequestParam("exerciseType") String exerciseType,
            @RequestParam(value = "stepsCount", required = false) Integer stepsCount,
            @RequestParam("durationMinutes") Integer durationMinutes,
            @RequestParam(value = "caloriesBurned", required = false) Double caloriesBurned,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Patient patient = getCurrentPatient();

        // Kiểm tra xem yêu cầu có phải là AJAX (fetch) hay không
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith) ||
                (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

        try {
            ExerciseLog savedLog = exerciseLogService.saveExerciseLog(
                    patient.getId(),
                    exerciseType,
                    stepsCount,
                    durationMinutes,
                    caloriesBurned
            );

            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Ghi nhận bài tập thành công!");
                response.put("exerciseId", savedLog.getId());
                return ResponseEntity.ok(response);
            }

            return "redirect:/patient/exercise";

        } catch (IllegalArgumentException ex) {
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", ex.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/patient/exercise";
        }
    }

    @PostMapping("/exercise/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteExercise(
            @PathVariable("id") Integer id,
            HttpServletRequest request) {

        Patient patient = getCurrentPatient();

        try {
            exerciseLogService.deleteExerciseLog(id, patient.getId());

            // Tính toán lại chỉ số hôm nay để cập nhật động ở giao diện
            Map<String, Object> summary = exerciseLogService.getTodaySummary(patient.getId());
            int streak = exerciseLogService.getCurrentStreak(patient.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xóa bài tập thành công!");
            response.put("totalMinutes", summary.get("totalMinutes"));
            response.put("totalCaloriesBurned", summary.get("totalCaloriesBurned"));
            response.put("targetMinutes", summary.get("targetMinutes"));
            response.put("exerciseProgress", summary.get("exerciseProgress"));
            response.put("streak", streak);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PostMapping("/exercise/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateExercise(
            @PathVariable("id") Integer id,
            @RequestParam("exerciseType") String exerciseType,
            @RequestParam(value = "stepsCount", required = false) Integer stepsCount,
            @RequestParam("durationMinutes") Integer durationMinutes,
            @RequestParam(value = "caloriesBurned", required = false) Double caloriesBurned,
            HttpServletRequest request) {

        Patient patient = getCurrentPatient();

        try {
            ExerciseLog updatedLog = exerciseLogService.updateExerciseLog(
                    id,
                    patient.getId(),
                    exerciseType,
                    stepsCount,
                    durationMinutes,
                    caloriesBurned
            );

            // Tính toán lại chỉ số hôm nay và streak để cập nhật động ở giao diện
            Map<String, Object> todaySummary = exerciseLogService.getTodaySummary(patient.getId());
            int streak = exerciseLogService.getCurrentStreak(patient.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật bài tập thành công!");
            response.put("exerciseId", updatedLog.getId());
            response.put("exerciseType", updatedLog.getExerciseType());
            response.put("durationMinutes", updatedLog.getDurationMinutes());
            response.put("stepsCount", updatedLog.getStepsCount());
            response.put("caloriesBurned", updatedLog.getCaloriesBurned());
            response.put("totalMinutes", todaySummary.get("totalMinutes"));
            response.put("totalCaloriesBurned", todaySummary.get("totalCaloriesBurned"));
            response.put("targetMinutes", todaySummary.get("targetMinutes"));
            response.put("exerciseProgress", todaySummary.get("exerciseProgress"));
            response.put("streak", streak);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
