package com.rpm.remotepatientmonitoring.controller.doctor;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.doctor.TreatmentPlanWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/doctor")
public class DoctorViewController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DiseaseProfileRepository diseaseProfileRepository;

    @Autowired
    private NutritionRuleRepository nutritionRuleRepository;

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private PatientMedicationRepository patientMedicationRepository;

    @Autowired
    private TreatmentPlanWorkflowService treatmentPlanWorkflowService;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private PatientExerciseRepository patientExerciseRepository;

    @Autowired
    private WaterLogRepository waterLogRepository;

    @ModelAttribute
    public void addNotificationAttributes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            doctorRepository.findByAccountId(userDetails.getAccount().getId()).ifPresent(doctor -> {
                long unreadNotificationsCount = notificationRepository.countByDoctorIdAndIsReadFalse(doctor.getId());
                List<Notification> recentNotifications = notificationRepository.findTop5ByDoctorIdOrderByCreatedAtDesc(doctor.getId());
                model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);
                model.addAttribute("recentNotifications", recentNotifications);
            });
        }
    }

    // =========================================================
    // 1. Dashboard: Hiển thị, Tìm kiếm, Phân trang
    // =========================================================
    @GetMapping("/dashboard")
    public String showDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {

        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            patientPage = patientRepository.searchPatientsForDoctor(doctor.getId(), keyword.trim(), pageable);
        } else {
            patientPage = patientRepository.findPatientsSortedByAlerts(doctor.getId(), pageable);
        }

        // 1. Tính tổng số bệnh nhân thực tế của bác sĩ này
        long actualPatientCount = patientRepository.countByDoctorId(doctor.getId());

        // 2. Tính số lượng cảnh báo đỏ và vàng chưa xử lý
        long redAlertsCount = alertRepository.countUnresolvedAlertsByColor(doctor.getId(), "RED");
        long yellowAlertsCount = alertRepository.countUnresolvedAlertsByColor(doctor.getId(), "YELLOW");

        // 3. Lấy chỉ số đo mới nhất của từng bệnh nhân trong trang hiện tại và tính cảnh báo cao nhất
        Map<Integer, DailyHealthLog> latestLogs = new HashMap<>();
        Map<Integer, String> patientHighestAlerts = new HashMap<>();
        
        for (Patient p : patientPage.getContent()) {
            healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(p.getId())
                    .ifPresent(log -> latestLogs.put(p.getId(), log));
            
            // Tìm cảnh báo nghiêm trọng nhất chưa xử lý
            List<Alert> alerts = alertRepository.findByPatientIdAndIsResolvedFalse(p.getId());
            String highestColor = "NONE";
            int maxSeverity = 0;
            for (Alert a : alerts) {
                int severity = "RED".equalsIgnoreCase(a.getAlertColor()) ? 4 
                             : "ORANGE".equalsIgnoreCase(a.getAlertColor()) ? 3 
                             : "YELLOW".equalsIgnoreCase(a.getAlertColor()) ? 2 : 1;
                if (severity > maxSeverity) {
                    maxSeverity = severity;
                    highestColor = a.getAlertColor().toUpperCase();
                }
            }
            patientHighestAlerts.put(p.getId(), highestColor);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("patientHighestAlerts", patientHighestAlerts);
        model.addAttribute("patientPage", patientPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("actualPatientCount", actualPatientCount);
        model.addAttribute("redAlertsCount", redAlertsCount);
        model.addAttribute("yellowAlertsCount", yellowAlertsCount);
        model.addAttribute("latestLogs", latestLogs);

        return "doctor/doctor-dashboard";
    }

    // =========================================================
    // 2. Trang Tiếp nhận bệnh nhân mới
    // =========================================================
    @GetMapping("/assign")
    public String showAssignPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        model.addAttribute("doctor", doctor);

        return "doctor/assign-patient";
    }

    // =========================================================
    // 3. GET: Xem & Cấu hình hồ sơ chi tiết bệnh nhân
    // =========================================================
    @GetMapping("/patient-detail/{id}")
    public String showPatientDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam(value = "success", required = false) String success,
            Model model) {

        // Lấy đúng bác sĩ từ session Spring Security — không hardcode ID
        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        // Lấy bệnh nhân theo path ID
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        // Kiểm tra bảo mật (Chống IDOR): Bệnh nhân phải thuộc quyền quản lý của bác sĩ đang đăng nhập
        if (patient.getDoctor() == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập hồ sơ của bệnh nhân này!");
        }

        // Toàn bộ hồ sơ bệnh lý để render radio buttons từ DB
        List<DiseaseProfile> allProfiles = diseaseProfileRepository.findAll();

        // Quy tắc dinh dưỡng hiện hành của bệnh nhân (để pre-fill form)
        NutritionRule currentRule = nutritionRuleRepository
                .findByPatientIdAndIsCurrent(patient.getId(), true)
                .orElse(null);

        TreatmentPlan currentPlan = treatmentPlanRepository
                .findByPatientIdAndIsCurrent(patient.getId(), true)
                .orElse(null);

        List<PatientMedication> currentMeds = patientMedicationRepository
                .findByPatientIdAndIsActiveTrue(patient.getId());

        // --- Bắt đầu tính toán cho phần Biểu đồ & Tuân thủ (7 ngày qua) ---
        LocalDate startDate = LocalDate.now().minusDays(7);
        
        // 1. Biểu đồ Sinh tồn (Huyết áp & Đường huyết)
        List<DailyHealthLog> healthLogs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);
        List<String> chartDates = healthLogs.stream().map(log -> log.getLogDate().toString()).toList();
        List<Integer> sysBpData = healthLogs.stream().map(DailyHealthLog::getSystolicBp).toList();
        List<Integer> diaBpData = healthLogs.stream().map(DailyHealthLog::getDiastolicBp).toList();
        List<BigDecimal> glucoseData = healthLogs.stream().map(DailyHealthLog::getGlucoseLevel).toList();
        
        // 2. Tuân thủ uống thuốc
        List<MedicationLog> medLogs = medicationLogRepository.findByPatientMedicationPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);
        long totalMeds = medLogs.size();
        long takenMeds = medLogs.stream().filter(MedicationLog::getIsTaken).count();
        int medCompliance = totalMeds > 0 ? (int) ((takenMeds * 100) / totalMeds) : 0;
        
        // 3. Tuân thủ uống nước
        List<WaterLog> waterLogs = waterLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);
        double totalWaterPercentage = 0;
        int dailyWaterTarget = (currentRule != null && currentRule.getDailyWaterMl() != null) ? currentRule.getDailyWaterMl() : 2000;
        for (WaterLog w : waterLogs) {
            if (w.getAmountMl() != null) {
                totalWaterPercentage += Math.min(100.0, (w.getAmountMl() * 100.0) / dailyWaterTarget);
            }
        }
        int waterCompliance = waterLogs.isEmpty() ? 0 : (int) (totalWaterPercentage / waterLogs.size());
        
        // 4. Tuân thủ vận động (Tối thiểu 1 hoạt động/ngày)
        List<PatientExercise> exerciseLogs = patientExerciseRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);
        long daysExercised = exerciseLogs.stream().map(PatientExercise::getLogDate).distinct().count();
        int exerciseCompliance = (int) ((daysExercised * 100) / 7);
        // --- Kết thúc tính toán ---

        model.addAttribute("doctor", doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("allProfiles", allProfiles);
        model.addAttribute("currentRule", currentRule);
        model.addAttribute("currentPlan", currentPlan);
        model.addAttribute("currentMeds", currentMeds);
        
        // Data biểu đồ
        model.addAttribute("chartDates", chartDates);
        model.addAttribute("sysBpData", sysBpData);
        model.addAttribute("diaBpData", diaBpData);
        model.addAttribute("glucoseData", glucoseData);
        
        // Data tuân thủ
        model.addAttribute("medCompliance", medCompliance);
        model.addAttribute("waterCompliance", waterCompliance);
        model.addAttribute("exerciseCompliance", exerciseCompliance);
        
        model.addAttribute("success", success);

        return "doctor/patient-detail";
    }

    // =========================================================
    // 4. POST: Lưu Phân loại Bệnh lý + Phác đồ + Dinh dưỡng + Thuốc
    // =========================================================
    @PostMapping("/patient-detail/{id}/update-profile")
    public String updatePatientProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam("diseaseProfileId") Integer diseaseProfileId,
            // Baseline measurements
            @RequestParam(value = "baselineSystolicBp", required = false) Integer baselineSystolicBp,
            @RequestParam(value = "baselineDiastolicBp", required = false) Integer baselineDiastolicBp,
            @RequestParam(value = "baselineFastingGlucose", required = false) BigDecimal baselineFastingGlucose,
            @RequestParam(value = "baselineHba1c", required = false) BigDecimal baselineHba1c,
            @RequestParam(value = "baselineWeightKg", required = false) BigDecimal baselineWeightKg,
            // Target measurements
            @RequestParam(value = "targetSystolicBp", required = false) Integer targetSystolicBp,
            @RequestParam(value = "targetDiastolicBp", required = false) Integer targetDiastolicBp,
            @RequestParam(value = "targetFastingGlucose", required = false) BigDecimal targetFastingGlucose,
            @RequestParam(value = "targetHba1c", required = false) BigDecimal targetHba1c,
            @RequestParam(value = "targetWeightKg", required = false) BigDecimal targetWeightKg,
            // Orders & Goals
            @RequestParam(value = "medicalOrder", required = false) String medicalOrder,
            @RequestParam(value = "exerciseGoal", required = false) String exerciseGoal,
            @RequestParam(value = "treatmentNotes", required = false) String treatmentNotes,
            // Nutrition Rules
            @RequestParam("maxCaloriesPerDay") Integer maxCaloriesPerDay,
            @RequestParam("maxCarbsG") BigDecimal maxCarbsG,
            @RequestParam("maxSaltG") BigDecimal maxSaltG,
            @RequestParam(value = "minFiberG", defaultValue = "25") BigDecimal minFiberG,
            @RequestParam(value = "maxFatG", required = false) BigDecimal maxFatG,
            @RequestParam(value = "minProteinG", required = false) BigDecimal minProteinG,
            @RequestParam(value = "dailyWaterMl", defaultValue = "2000") Integer dailyWaterMl,
            @RequestParam(value = "additionalNotes", required = false) String additionalNotes,
            // Medications
            @RequestParam(value = "medNames", required = false) List<String> medNames,
            @RequestParam(value = "medDosages", required = false) List<String> medDosages,
            @RequestParam(value = "medScheduledTimes", required = false) List<String> medScheduledTimes
    ) {

        // Xác thực bác sĩ qua Spring Security
        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        // Lấy bệnh nhân
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        // Kiểm tra bảo mật (Chống IDOR): Bệnh nhân phải thuộc quyền quản lý của bác sĩ đang đăng nhập
        if (patient.getDoctor() == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Bạn không có quyền cập nhật hồ sơ của bệnh nhân này!");
        }

        // --- Cập nhật phân loại bệnh lý trực tiếp ---
        DiseaseProfile selectedProfile = new DiseaseProfile();
        selectedProfile.setId(diseaseProfileId);
        patient.setDiseaseProfile(selectedProfile);
        patientRepository.save(patient);

        // --- Gọi workflow service xử lý phác đồ, quy tắc dinh dưỡng, thuốc và gửi thông báo ---
        treatmentPlanWorkflowService.createNewTreatmentPlan(
                patient,
                doctor,
                baselineSystolicBp,
                baselineDiastolicBp,
                baselineFastingGlucose,
                baselineHba1c,
                baselineWeightKg,
                targetSystolicBp,
                targetDiastolicBp,
                targetFastingGlucose,
                targetHba1c,
                targetWeightKg,
                medicalOrder != null ? medicalOrder : "",
                exerciseGoal,
                treatmentNotes,
                maxCaloriesPerDay,
                maxCarbsG,
                maxSaltG,
                minFiberG,
                maxFatG,
                minProteinG,
                dailyWaterMl,
                additionalNotes,
                medNames,
                medDosages,
                medScheduledTimes
        );

        // Redirect về trang chi tiết với flag thành công
        return "redirect:/doctor/patient-detail/" + id + "?success=true";
    }

    // =========================================================
    // 5. GET: Trang hồ sơ bác sĩ
    // =========================================================
    @GetMapping("/profile")
    public String showProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        long actualPatientCount = patientRepository.countByDoctorId(doctor.getId());

        // Fetch eagerly để tránh LazyInitializationException trong Thymeleaf
        String doctorEmail  = doctor.getAccount()  != null ? doctor.getAccount().getEmail()  : "N/A";
        String hospitalName = doctor.getHospital() != null ? doctor.getHospital().getFullName() : "N/A";

        model.addAttribute("doctor", doctor);
        model.addAttribute("actualPatientCount", actualPatientCount);
        model.addAttribute("doctorEmail", doctorEmail);
        model.addAttribute("hospitalName", hospitalName);

        return "doctor/doctor-profile";
    }

    // =========================================================
    // 6. POST: Đổi mật khẩu bác sĩ
    // =========================================================
    @PostMapping("/profile/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword")     String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        Integer accountId = userDetails.getAccount().getId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        // 1. Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu hiện tại không đúng!");
            return "redirect:/doctor/profile";
        }

        // 2. Kiểm tra xác nhận
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu xác nhận không khớp!");
            return "redirect:/doctor/profile";
        }

        // 3. Kiểm tra độ dài tối thiểu
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu mới phải có ít nhất 8 ký tự!");
            return "redirect:/doctor/profile";
        }

        // 4. Lưu mật khẩu mới
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        redirectAttributes.addFlashAttribute("successMsg", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
        return "redirect:/doctor/profile";
    }
}
