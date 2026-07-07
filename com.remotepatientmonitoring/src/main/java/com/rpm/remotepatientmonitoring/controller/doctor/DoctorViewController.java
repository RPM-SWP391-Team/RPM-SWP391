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
import java.time.Period;
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
    private com.rpm.remotepatientmonitoring.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private PatientExerciseRepository patientExerciseRepository;

    @Autowired
    private WaterLogRepository waterLogRepository;

    @Autowired
    private ChangeRequestRepository changeRequestRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.service.doctor.AuditTrailService auditTrailService;

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
        // Chỉ lấy các bản ghi có đủ dữ liệu, tránh null gây lỗi Chart.js
        List<DailyHealthLog> bpLogs = healthLogs.stream()
                .filter(log -> log.getSystolicBp() != null && log.getDiastolicBp() != null)
                .toList();
        List<DailyHealthLog> glucoseLogs = healthLogs.stream()
                .filter(log -> log.getGlucoseLevel() != null)
                .toList();

        List<String> chartDates = bpLogs.stream().map(log -> log.getLogDate().toString()).toList();
        List<Integer> sysBpData = bpLogs.stream().map(DailyHealthLog::getSystolicBp).toList();
        List<Integer> diaBpData = bpLogs.stream().map(DailyHealthLog::getDiastolicBp).toList();
        List<String> glucoseDates = glucoseLogs.stream().map(log -> log.getLogDate().toString()).toList();
        List<BigDecimal> glucoseData = glucoseLogs.stream().map(DailyHealthLog::getGlucoseLevel).toList();
        
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

        int age = 0;
        if (patient.getDateOfBirth() != null) {
            age = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
        }

        // Fetch unresolved alerts
        List<com.rpm.remotepatientmonitoring.model.Alert> unresolvedAlerts = alertRepository.findByPatientIdAndIsResolvedFalse(patient.getId());

        model.addAttribute("patientAge", age);
        model.addAttribute("doctor", doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("allProfiles", allProfiles);
        model.addAttribute("currentRule", currentRule);
        model.addAttribute("currentPlan", currentPlan);
        model.addAttribute("unresolvedAlerts", unresolvedAlerts);
        model.addAttribute("currentMeds", currentMeds);
        
        // Data biểu đồ
        model.addAttribute("chartDates", chartDates);
        model.addAttribute("sysBpData", sysBpData);
        model.addAttribute("diaBpData", diaBpData);
        model.addAttribute("glucoseDates", glucoseDates);
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

    // =========================================================
    // 7. Quản lý Yêu cầu (Change Requests)
    // =========================================================
    @GetMapping("/change-requests")
    public String viewChangeRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails, 
            @RequestParam(value = "tab", defaultValue = "pending") String tab,
            Model model) {
        
        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return "redirect:/auth/login";
        }

        List<ChangeRequest> changeRequests;
        if ("history".equals(tab)) {
            changeRequests = changeRequestRepository.findByDoctorIdAndStatusNotOrderByCreatedAtDesc(doctor.getId(), "PENDING");
        } else {
            changeRequests = changeRequestRepository.findByDoctorIdAndStatusOrderByCreatedAtDesc(doctor.getId(), "PENDING");
        }
        
        long pendingCount = changeRequestRepository.findByDoctorIdAndStatusOrderByCreatedAtDesc(doctor.getId(), "PENDING").size();

        model.addAttribute("doctor", doctor);
        model.addAttribute("changeRequests", changeRequests);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("activeTab", tab);

        return "doctor/change-requests";
    }

    @PostMapping("/change-requests/{id}/process")
    public String processChangeRequest(
            @PathVariable("id") Integer id,
            @RequestParam("action") String action,
            @RequestParam("doctorResponse") String doctorResponse,
            RedirectAttributes redirectAttributes) {
        
        ChangeRequest request = changeRequestRepository.findById(id).orElse(null);
        if (request != null) {
            if ("APPROVE".equalsIgnoreCase(action)) {
                request.setStatus("APPROVED");
            } else if ("REJECT".equalsIgnoreCase(action)) {
                request.setStatus("REJECTED");
            }
            request.setDoctorResponse(doctorResponse);
            request.setProcessedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            changeRequestRepository.save(request);

            // Optional: send notification back to patient
            try {
                String reqTypeStr = request.getRequestType() != null ? request.getRequestType() : "";
                String title = "Phản hồi yêu cầu " + ("RESCHEDULE".equals(reqTypeStr) ? "đổi lịch" : "đổi phác đồ");
                String content = "Bác sĩ đã " + ("APPROVED".equals(request.getStatus()) ? "duyệt" : "từ chối") + " yêu cầu của bạn: " + (doctorResponse != null ? doctorResponse : "");
                
                Notification notif = Notification.builder()
                        .patient(request.getPatient())
                        .doctor(request.getDoctor())
                        .title(title)
                        .content(content)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notif);
            } catch (Exception e) {
                System.out.println("Error saving notification: " + e.getMessage());
            }

            // Ghi Audit Trail
            auditTrailService.logAction(
                    "DOCTOR",
                    request.getDoctor() != null ? request.getDoctor().getId() : null,
                    "PROCESS_CHANGE_REQUEST",
                    "change_requests",
                    request.getId(),
                    null,
                    request,
                    "Bác sĩ " + action + " yêu cầu thay đổi với ghi chú: " + doctorResponse
            );

            redirectAttributes.addFlashAttribute("successMsg", "Đã xử lý yêu cầu thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy yêu cầu này.");
        }

        return "redirect:/doctor/change-requests";
    }
        // =========================================================
    // 8. Quản lý Lịch hẹn (Appointments)
    // =========================================================
    @GetMapping("/appointments")
    public String viewAppointments(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @RequestParam(value = "tab", defaultValue = "upcoming") String tab,
                                   @RequestParam(value = "startDate", required = false) String startDateStr,
                                   @RequestParam(value = "endDate", required = false) String endDateStr,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "size", defaultValue = "5") int size,
                                   Model model) {
        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return "redirect:/auth/login";
        }

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);
        Page<com.rpm.remotepatientmonitoring.model.Appointment> appointmentPage;
        
        LocalDateTime filterStart = null;
        LocalDateTime filterEnd = null;
        
        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                filterStart = LocalDate.parse(startDateStr.trim()).atStartOfDay();
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                filterEnd = LocalDate.parse(endDateStr.trim()).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            // ignore invalid date format
        }

        // Nếu chỉ nhập 1 trong 2 thì gán bằng nhau để vẫn tìm được trong 1 ngày đó
        if (filterStart != null && filterEnd == null) {
            filterEnd = filterStart.toLocalDate().atTime(23, 59, 59);
        } else if (filterStart == null && filterEnd != null) {
            filterStart = filterEnd.toLocalDate().atStartOfDay();
        }

        if ("history".equals(tab)) {
            if (filterStart != null && filterEnd != null) {
                appointmentPage = appointmentRepository.findHistoryByDate(doctor.getId(), filterStart, filterEnd, now, pageable);
            } else {
                appointmentPage = appointmentRepository.findHistory(doctor.getId(), now, pageable);
            }
        } else {
            if (filterStart != null && filterEnd != null) {
                appointmentPage = appointmentRepository.findUpcomingByDate(doctor.getId(), filterStart, filterEnd, now, pageable);
            } else {
                appointmentPage = appointmentRepository.findUpcoming(doctor.getId(), now, pageable);
            }
        }

        long upcomingCount = appointmentRepository.countUpcoming(doctor.getId(), now);

        model.addAttribute("doctor", doctor);
        model.addAttribute("patients", patientRepository.findByDoctorIdAndIsActiveTrue(doctor.getId()));
        
        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("activeTab", tab);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);

        return "doctor/appointments";
    }

    @PostMapping("/appointments/create")
    public String createAppointment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam("patientId") Integer patientId,
                                    @RequestParam("appointmentTime") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime apptTime,
                                    @RequestParam(value = "location", required = false) String location,
                                    @RequestParam(value = "doctorNote", required = false) String doctorNote,
                                    RedirectAttributes redirectAttributes) {
        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return "redirect:/auth/login";
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể lên lịch cho bệnh nhân này.");
            return "redirect:/doctor/appointments";
        }

        try {
            if (apptTime.isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMsg", "Thời gian hẹn phải ở trong tương lai.");
                return "redirect:/doctor/appointments";
            }

            com.rpm.remotepatientmonitoring.model.Appointment appt = com.rpm.remotepatientmonitoring.model.Appointment.builder()
                    .patient(patient)
                    .doctor(doctor)
                    .appointmentTime(apptTime)
                    .location(location)
                    .doctorNote(doctorNote)
                    .appointmentType("FOLLOWUP")
                    .status("ACCEPTED") // Bác sĩ tự tạo nên đã chốt
                    .createdBy("DOCTOR")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            appointmentRepository.save(appt);

            // Gửi thông báo cho bệnh nhân
            Notification notif = Notification.builder()
                    .patient(patient)
                    .doctor(doctor)
                    .title("Lịch khám mới được lên bởi Bác sĩ")
                    .content("Bác sĩ " + doctor.getFullName() + " đã xếp lịch tái khám vào " + apptTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + (location != null && !location.isEmpty() ? " tại " + location : "") + ".")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);

            redirectAttributes.addFlashAttribute("successMsg", "Đã lên lịch khám thành công cho bệnh nhân " + patient.getFullName() + ".");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi tạo lịch khám: " + e.getMessage() + (e.getCause() != null ? " - " + e.getCause().getMessage() : ""));
        }

        return "redirect:/doctor/appointments";
    }

    // =========================================================
    // ALERT HANDLING
    // =========================================================
    @PostMapping("/alerts/{id}/resolve")
    public String resolveAlert(
            @PathVariable("id") Integer id,
            @RequestParam("resolutionNotes") String resolutionNotes,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId).orElse(null);
        if (doctor == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy phiên bác sĩ.");
            return "redirect:/doctor/dashboard";
        }

        com.rpm.remotepatientmonitoring.model.Alert alert = alertRepository.findById(id).orElse(null);
        if (alert != null) {
            alert.setIsResolved(true);
            alert.setResolvedAt(java.time.LocalDateTime.now());
            alert.setResolvedByDoctor(doctor);
            alert.setResolutionNotes(resolutionNotes);
            alertRepository.save(alert);

            // Ghi Audit Trail
            auditTrailService.logAction(
                    "DOCTOR",
                    doctor.getId(),
                    "RESOLVE_ALERT",
                    "alerts",
                    alert.getId(),
                    null,
                    alert,
                    "Bác sĩ " + doctor.getFullName() + " xử lý cảnh báo với ghi chú: " + resolutionNotes
            );

            redirectAttributes.addFlashAttribute("successMsg", "Đã xử lý cảnh báo y tế.");
            return "redirect:/doctor/patient-detail/" + alert.getPatient().getId() + "?success=alert-resolved";
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy cảnh báo.");
            return "redirect:/doctor/dashboard";
        }
    }
}
