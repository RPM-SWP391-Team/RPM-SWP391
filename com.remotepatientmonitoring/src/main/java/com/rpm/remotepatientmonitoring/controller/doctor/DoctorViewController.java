package com.rpm.remotepatientmonitoring.controller.doctor;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.doctor.TreatmentPlanWorkflowService;
import com.rpm.remotepatientmonitoring.service.RatingService;
import com.rpm.remotepatientmonitoring.dto.hopital.AlertThresholdsDTO;
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
@lombok.RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class DoctorViewController {

    private static final String MSG_DOCTOR_NOT_FOUND = "Không tìm thấy bác sĩ";
    private static final String ATTR_ERROR_MSG = "errorMsg";
    private static final String ATTR_SUCCESS_MSG = "successMsg";
    private static final String REDIRECT_LOGIN = "redirect:/auth/login";
    private static final String REDIRECT_PROFILE = "redirect:/doctor/profile";
    private static final String REDIRECT_DASHBOARD = "redirect:/doctor/dashboard";
    private static final String REDIRECT_APPOINTMENTS = "redirect:/doctor/appointments";
    private static final String REDIRECT_CHANGE_REQUESTS = "redirect:/doctor/change-requests";
    private static final String REDIRECT_NOTIFICATIONS = "redirect:/doctor/notifications";
    private static final String MSG_APPOINTMENT_NOT_FOUND = "Không tìm thấy lịch hẹn.";
    private static final String ATTR_DOCTOR = "doctor";

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DiseaseProfileRepository diseaseProfileRepository;
    private final NutritionRuleRepository nutritionRuleRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final PatientMedicationRepository patientMedicationRepository;
    private final TreatmentPlanWorkflowService treatmentPlanWorkflowService;
    private final AlertRepository alertRepository;
    private final HealthLogRepository healthLogRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.rpm.remotepatientmonitoring.repository.AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final PatientExerciseRepository patientExerciseRepository;
    private final WaterLogRepository waterLogRepository;
    private final RatingService ratingService;
    private final ChangeRequestRepository changeRequestRepository;
    private final AlertThresholdRepository alertThresholdRepository;
    private final com.rpm.remotepatientmonitoring.service.doctor.AuditTrailService auditTrailService;

    @ModelAttribute
    public void addNotificationAttributes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            doctorRepository.findByAccountId(userDetails.getAccount().getId()).ifPresent(doctor -> {
                long unreadNotificationsCount = notificationRepository.countByDoctorIdAndIsReadFalse(doctor.getId());
                model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);
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
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

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
            
            List<Alert> alerts = alertRepository.findByPatientIdAndIsResolvedFalse(p.getId());
            patientHighestAlerts.put(p.getId(), calculateHighestAlertColor(alerts));
        }

        model.addAttribute(ATTR_DOCTOR, doctor);
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
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

        model.addAttribute(ATTR_DOCTOR, doctor);
        model.addAttribute("diseaseProfiles", diseaseProfileRepository.findAll());

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
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

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

        prepareChartAndComplianceData(model, patient, currentRule);
        
        model.addAttribute(ATTR_DOCTOR, doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("allProfiles", allProfiles);
        model.addAttribute("currentRule", currentRule);
        model.addAttribute("currentPlan", currentPlan);
        model.addAttribute("currentMeds", currentMeds);
        model.addAttribute("success", success);

        // Lấy cấu hình ngưỡng cảnh báo
        AlertThreshold thresholdEntity = alertThresholdRepository.findByPatientIdAndScope(patient.getId(), "PATIENT")
                .orElse(null);
        boolean hasCustomThreshold = true;

        if (thresholdEntity == null) {
            hasCustomThreshold = false;
            if (doctor.getHospital() != null) {
                thresholdEntity = alertThresholdRepository.findByHospitalIdAndScope(doctor.getHospital().getId(), "HOSPITAL")
                        .orElse(null);
            }
        }

        AlertThresholdsDTO thresholdDTO = new AlertThresholdsDTO();
        if (thresholdEntity != null) {
            thresholdDTO.setId(thresholdEntity.getId());
            thresholdDTO.setGlucoseHypoThreshold(thresholdEntity.getGlucoseHypoThreshold());
            thresholdDTO.setGlucoseNormalMax(thresholdEntity.getGlucoseNormalMax());
            thresholdDTO.setGlucoseHighMax(thresholdEntity.getGlucoseHighMax());
            thresholdDTO.setSystolicNormalMax(thresholdEntity.getSystolicNormalMax());
            thresholdDTO.setSystolicWarningMin(thresholdEntity.getSystolicWarningMin());
            thresholdDTO.setSystolicWarningMax(thresholdEntity.getSystolicWarningMax());
            thresholdDTO.setSystolicDangerMin(thresholdEntity.getSystolicDangerMin());
            thresholdDTO.setSystolicDangerMax(thresholdEntity.getSystolicDangerMax());
            thresholdDTO.setSystolicEmergencyThreshold(thresholdEntity.getSystolicEmergencyThreshold());
            thresholdDTO.setDiastolicNormalMax(thresholdEntity.getDiastolicNormalMax());
            thresholdDTO.setDiastolicWarningMin(thresholdEntity.getDiastolicWarningMin());
            thresholdDTO.setDiastolicWarningMax(thresholdEntity.getDiastolicWarningMax());
            thresholdDTO.setDiastolicDangerMin(thresholdEntity.getDiastolicDangerMin());
            thresholdDTO.setDiastolicDangerMax(thresholdEntity.getDiastolicDangerMax());
            thresholdDTO.setDiastolicEmergencyThreshold(thresholdEntity.getDiastolicEmergencyThreshold());
        }

        model.addAttribute("threshold", thresholdDTO);
        model.addAttribute("hasCustomThreshold", hasCustomThreshold);

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
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

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
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

        long actualPatientCount = patientRepository.countByDoctorId(doctor.getId());

        // Fetch eagerly để tránh LazyInitializationException trong Thymeleaf
        String doctorEmail  = doctor.getAccount()  != null ? doctor.getAccount().getEmail()  : "N/A";
        String hospitalName = doctor.getHospital() != null ? doctor.getHospital().getFullName() : "N/A";

        ratingService.populateDoctorRatings(doctor);

        model.addAttribute(ATTR_DOCTOR, doctor);
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
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Mật khẩu hiện tại không đúng!");
            return REDIRECT_PROFILE;
        }

        // 2. Kiểm tra xác nhận
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Mật khẩu xác nhận không khớp!");
            return REDIRECT_PROFILE;
        }
        // 3. Kiểm tra độ dài tối thiểu
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Mật khẩu mới phải có ít nhất 8 ký tự!");
            return REDIRECT_PROFILE;
        }

        // 4. Lưu mật khẩu mới
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
        return REDIRECT_PROFILE;
    }

    // =========================================================
    // 7. Quản lý Yêu cầu (Change Requests)
    // =========================================================
    @GetMapping("/change-requests")
    public String viewChangeRequests(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestParam(value = "tab", defaultValue = "pending") String tab,
                                     @RequestParam(value = "startDate", required = false) String startDateStr,
                                     @RequestParam(value = "endDate", required = false) String endDateStr,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "5") int size,
                                     Model model) {
        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return REDIRECT_LOGIN;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ChangeRequest> changeRequestPage;

        LocalDateTime[] dates = parseDateParameters(startDateStr, endDateStr);
        LocalDateTime filterStart = dates[0];
        LocalDateTime filterEnd = dates[1];

        if ("history".equals(tab)) {
            if (filterStart != null && filterEnd != null) {
                changeRequestPage = changeRequestRepository.findHistoryByDate(doctor.getId(), filterStart, filterEnd, pageable);
            } else {
                changeRequestPage = changeRequestRepository.findHistory(doctor.getId(), pageable);
            }
        } else {
            if (filterStart != null && filterEnd != null) {
                changeRequestPage = changeRequestRepository.findPendingByDate(doctor.getId(), filterStart, filterEnd, pageable);
            } else {
                changeRequestPage = changeRequestRepository.findPending(doctor.getId(), pageable);
            }
        }

        long pendingCount = changeRequestRepository.findByDoctorIdAndStatusOrderByCreatedAtDesc(doctor.getId(), "PENDING").size();

        model.addAttribute(ATTR_DOCTOR, doctor);
        model.addAttribute("changeRequestPage", changeRequestPage);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("activeTab", tab);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);

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
                        .recipientType("PATIENT")
                        .title(title)
                        .content(content)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(notif);
            } catch (Exception e) {
                log.error("Error saving notification: " + e.getMessage());
            }

            redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã xử lý yêu cầu thành công!");
        } else {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Không tìm thấy yêu cầu này.");
        }

        return REDIRECT_CHANGE_REQUESTS;
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
            return REDIRECT_LOGIN;
        }

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);
        Page<com.rpm.remotepatientmonitoring.model.Appointment> appointmentPage;
        
        LocalDateTime[] dates = parseDateParameters(startDateStr, endDateStr);
        LocalDateTime filterStart = dates[0];
        LocalDateTime filterEnd = dates[1];

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

        model.addAttribute(ATTR_DOCTOR, doctor);
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
                                    @RequestParam("appointmentTime") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime apptTime,
                                    @RequestParam(value = "location", required = true) String location,
                                    @RequestParam(value = "doctorNote", required = false) String doctorNote,
                                    RedirectAttributes redirectAttributes) {
        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return REDIRECT_LOGIN;
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Không thể lên lịch cho bệnh nhân này.");
            return REDIRECT_APPOINTMENTS;
        }

        try {
            if (apptTime.isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Thời gian hẹn phải ở trong tương lai.");
                return REDIRECT_APPOINTMENTS;
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
                    .recipientType("PATIENT")
                    .title("Lịch khám mới được lên bởi Bác sĩ")
                    .content("Bác sĩ " + doctor.getFullName() + " đã xếp lịch tái khám vào " + apptTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + (location != null && !location.isEmpty() ? " tại " + location : "") + ".")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);

            redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã lên lịch khám thành công cho bệnh nhân " + patient.getFullName() + ".");
        } catch (Exception e) {
            log.error("Exception: ", e);
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Lỗi tạo lịch khám: " + e.getMessage() + (e.getCause() != null ? " - " + e.getCause().getMessage() : ""));
        }

        return REDIRECT_APPOINTMENTS;
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
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Không tìm thấy phiên bác sĩ.");
            return REDIRECT_DASHBOARD;
        }

        com.rpm.remotepatientmonitoring.model.Alert alert = alertRepository.findById(id).orElse(null);
        if (alert != null) {
            alert.setIsResolved(true);
            alert.setResolvedAt(java.time.LocalDateTime.now());
            alert.setResolvedByDoctor(doctor);
            alert.setResolutionNotes(resolutionNotes);
            alertRepository.save(alert);

            // Ghi Audit Trail
            if (auditTrailService != null) {
                try {
                    java.util.Map<String, Object> aLog = new java.util.HashMap<>();
                    aLog.put("alertId", alert.getId());
                    aLog.put("alertLevel", alert.getAlertLevel() != null ? alert.getAlertLevel() : (alert.getAlertColor() != null ? alert.getAlertColor() : "LEVEL 3"));
                    aLog.put("patientId", alert.getPatient() != null ? alert.getPatient().getId() : null);
                    aLog.put("patientName", alert.getPatient() != null ? alert.getPatient().getFullName() : "Bệnh nhân");
                    aLog.put("status", "RESOLVED");
                    aLog.put("resolutionNotes", resolutionNotes);

                    auditTrailService.logAction(
                            "DOCTOR",
                            doctor.getId(),
                            "RESOLVE_ALERT",
                            "alerts",
                            alert.getId(),
                            null,
                            aLog,
                            "Bác sĩ xác nhận đã kiểm tra và xử lý cảnh báo"
                    );
                } catch (Exception e) {
                    // ignore
                }
            }

            redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã xử lý cảnh báo y tế.");
            return "redirect:/doctor/patient-detail/" + alert.getPatient().getId() + "?success=alert-resolved";
        } else {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Không tìm thấy cảnh báo.");
            return REDIRECT_DASHBOARD;
        }
    }
    // =========================================================
    // 10. NOTIFICATIONS
    // =========================================================
    @GetMapping("/notifications")
    public String viewNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return REDIRECT_LOGIN;
        }

        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime[] dates = parseDateParameters(startDateStr, endDateStr);
        LocalDateTime filterStart = dates[0];
        LocalDateTime filterEnd = dates[1];

        Page<Notification> notificationPage;
        if (filterStart != null && filterEnd != null) {
            notificationPage = notificationRepository.findByDoctorIdAndRecipientTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                    doctor.getId(), filterStart, filterEnd, pageable);
        } else {
            notificationPage = notificationRepository.findByDoctorIdAndRecipientTypeOrderByCreatedAtDesc(doctor.getId(), pageable);
        }

        model.addAttribute(ATTR_DOCTOR, doctor);
        model.addAttribute("notificationPage", notificationPage);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);

        return "doctor/notifications";
    }

    @GetMapping("/notifications/{id}/read")
    public String markNotificationAsRead(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return REDIRECT_LOGIN;
        }

        Notification notif = notificationRepository.findById(id).orElse(null);
        if (notif == null 
                || notif.getDoctor() == null 
                || !notif.getDoctor().getId().equals(doctor.getId())
                || !"DOCTOR".equals(notif.getRecipientType())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Không tìm thấy thông báo.");
            return REDIRECT_NOTIFICATIONS;
        }

        notif.setIsRead(true);
        notificationRepository.save(notif);

        if (notif.getPatient() != null) {
            return "redirect:/doctor/patient-detail/" + notif.getPatient().getId();
        }
        return REDIRECT_NOTIFICATIONS;
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor != null) {
            notificationRepository.markAllAsReadByDoctorId(doctor.getId());
        }
        return REDIRECT_NOTIFICATIONS;
    }

    private String calculateHighestAlertColor(List<Alert> alerts) {
        String highestColor = "NONE";
        int maxSeverity = 0;
        for (Alert a : alerts) {
            int severity = getAlertSeverity(a.getAlertColor());
            if (severity > maxSeverity) {
                maxSeverity = severity;
                highestColor = a.getAlertColor().toUpperCase();
            }
        }
        return highestColor;
    }

    private int getAlertSeverity(String color) {
        if ("RED".equalsIgnoreCase(color)) return 4;
        if ("ORANGE".equalsIgnoreCase(color)) return 3;
        if ("YELLOW".equalsIgnoreCase(color)) return 2;
        return 1;
    }

    private void prepareChartAndComplianceData(Model model, Patient patient, NutritionRule currentRule) {
        LocalDate startDate = LocalDate.now().minusDays(7);
        
        List<DailyHealthLog> healthLogs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);
        List<DailyHealthLog> bpLogs = healthLogs.stream()
                .filter(log -> log.getSystolicBp() != null && log.getDiastolicBp() != null)
                .toList();
        List<DailyHealthLog> glucoseLogs = healthLogs.stream()
                .filter(log -> log.getGlucoseLevel() != null)
                .toList();

        model.addAttribute("chartDates", bpLogs.stream().map(log -> log.getLogDate().toString()).toList());
        model.addAttribute("sysBpData", bpLogs.stream().map(DailyHealthLog::getSystolicBp).toList());
        model.addAttribute("diaBpData", bpLogs.stream().map(DailyHealthLog::getDiastolicBp).toList());
        model.addAttribute("glucoseDates", glucoseLogs.stream().map(log -> log.getLogDate().toString()).toList());
        model.addAttribute("glucoseData", glucoseLogs.stream().map(DailyHealthLog::getGlucoseLevel).toList());
        
        List<MedicationLog> medLogs = medicationLogRepository.findByPatientMedicationPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);
        long totalMeds = medLogs.size();
        long takenMeds = medLogs.stream().filter(MedicationLog::getIsTaken).count();
        model.addAttribute("medCompliance", totalMeds > 0 ? (int) ((takenMeds * 100) / totalMeds) : 0);
        
        List<WaterLog> waterLogs = waterLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);
        double totalWaterPercentage = 0;
        int dailyWaterTarget = (currentRule != null && currentRule.getDailyWaterMl() != null) ? currentRule.getDailyWaterMl() : 2000;
        for (WaterLog w : waterLogs) {
            if (w.getAmountMl() != null) {
                totalWaterPercentage += Math.min(100.0, (w.getAmountMl() * 100.0) / dailyWaterTarget);
            }
        }
        model.addAttribute("waterCompliance", waterLogs.isEmpty() ? 0 : (int) (totalWaterPercentage / waterLogs.size()));
        
        List<PatientExercise> exerciseLogs = patientExerciseRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);
        long daysExercised = exerciseLogs.stream().map(PatientExercise::getLogDate).distinct().count();
        model.addAttribute("exerciseCompliance", (int) ((daysExercised * 100) / 7));

        int age = 0;
        if (patient.getDateOfBirth() != null) {
            age = Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears();
        }
        model.addAttribute("patientAge", age);
        
        model.addAttribute("unresolvedAlerts", alertRepository.findByPatientIdAndIsResolvedFalse(patient.getId()));
    }

    private LocalDateTime[] parseDateParameters(String startDateStr, String endDateStr) {
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
            // ignore
        }
        if (filterStart != null && filterEnd == null) {
            filterEnd = filterStart.toLocalDate().atTime(23, 59, 59);
        }
        if (filterStart == null && filterEnd != null) {
            filterStart = filterEnd.toLocalDate().atStartOfDay();
        }
        return new LocalDateTime[]{filterStart, filterEnd};
    }

    @PostMapping("/appointments/{id}/accept")
    public String acceptAppointment(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return REDIRECT_LOGIN;
        }

        Appointment appt = appointmentRepository.findById(id).orElse(null);
        if (appt == null || appt.getDoctor() == null || !appt.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, MSG_APPOINTMENT_NOT_FOUND);
            return REDIRECT_APPOINTMENTS;
        }

        if (!"PENDING".equals(appt.getStatus())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Lịch hẹn này đã được xử lý trước đó.");
            return REDIRECT_APPOINTMENTS;
        }

        appt.setStatus("ACCEPTED");
        appt.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appt);

        // Thông báo cho bệnh nhân
        if (appt.getPatient() != null) {
            Notification notif = Notification.builder()
                    .patient(appt.getPatient())
                    .doctor(doctor)
                    .recipientType("PATIENT")
                    .title("Lịch khám đã được chấp nhận")
                    .content("Bác sĩ " + doctor.getFullName() + " đã xác nhận lịch khám vào "
                            + appt.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        }

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã chấp nhận lịch hẹn.");
        return REDIRECT_APPOINTMENTS;
    }

    @PostMapping("/appointments/{id}/reject")
    public String rejectAppointment(
            @PathVariable("id") Integer id,
            @RequestParam("rejectionReason") String rejectionReason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return REDIRECT_LOGIN;
        }

        Appointment appt = appointmentRepository.findById(id).orElse(null);
        if (appt == null || appt.getDoctor() == null || !appt.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, MSG_APPOINTMENT_NOT_FOUND);
            return REDIRECT_APPOINTMENTS;
        }

        if (!"PENDING".equals(appt.getStatus())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Lịch hẹn này đã được xử lý trước đó.");
            return REDIRECT_APPOINTMENTS;
        }

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Vui lòng nhập lý do từ chối.");
            return REDIRECT_APPOINTMENTS;
        }

        appt.setStatus("REJECTED");
        appt.setDoctorNote(rejectionReason.trim()); // tái dùng field doctorNote sẵn có để lưu lý do từ chối
        appt.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appt);

        if (appt.getPatient() != null) {
            Notification notif = Notification.builder()
                    .patient(appt.getPatient())
                    .doctor(doctor)
                    .recipientType("PATIENT")
                    .title("Lịch khám đã bị từ chối")
                    .content("Bác sĩ " + doctor.getFullName() + " đã từ chối lịch khám vào "
                            + appt.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            + ". Lý do: " + rejectionReason.trim())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        }

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã từ chối lịch hẹn.");
        return REDIRECT_APPOINTMENTS;
    }

    // =========================================================
    // POST: Lưu Cấu hình Ngưỡng Cảnh Báo Riêng Cho Bệnh Nhân
    // =========================================================
    @PostMapping("/patient-detail/{id}/update-thresholds")
    public String updatePatientThresholds(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("threshold") AlertThresholdsDTO dto,
            RedirectAttributes redirectAttributes) {

        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId).orElse(null);
        if (doctor == null) return REDIRECT_LOGIN;

        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient == null || patient.getDoctor() == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Không tìm thấy bệnh nhân hoặc bạn không có quyền.");
            return REDIRECT_DASHBOARD;
        }

        if (doctor.getHospital() == null) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Bác sĩ chưa được liên kết với bệnh viện nào.");
            return REDIRECT_DASHBOARD;
        }

        // Validate logic khoảng cảnh báo Tâm thu
        if (dto.getSystolicNormalMax() >= dto.getSystolicWarningMin() ||
            dto.getSystolicWarningMin() > dto.getSystolicWarningMax() ||
            dto.getSystolicWarningMax() >= dto.getSystolicDangerMin() ||
            dto.getSystolicDangerMin() > dto.getSystolicDangerMax() ||
            dto.getSystolicDangerMax() >= dto.getSystolicEmergencyThreshold()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Logic khoảng huyết áp Tâm thu không hợp lệ (Cần tuân thủ: Bình thường < Cảnh báo < Nguy hiểm < Cấp cứu).");
            return "redirect:/doctor/patient-detail/" + id;
        }

        // Validate logic khoảng cảnh báo Tâm trương
        if (dto.getDiastolicNormalMax() >= dto.getDiastolicWarningMin() ||
            dto.getDiastolicWarningMin() > dto.getDiastolicWarningMax() ||
            dto.getDiastolicWarningMax() >= dto.getDiastolicDangerMin() ||
            dto.getDiastolicDangerMin() > dto.getDiastolicDangerMax() ||
            dto.getDiastolicDangerMax() >= dto.getDiastolicEmergencyThreshold()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Logic khoảng huyết áp Tâm trương không hợp lệ (Cần tuân thủ: Bình thường < Cảnh báo < Nguy hiểm < Cấp cứu).");
            return "redirect:/doctor/patient-detail/" + id;
        }

        // Validate logic khoảng đường huyết
        if (dto.getGlucoseHypoThreshold().compareTo(dto.getGlucoseNormalMax()) >= 0 ||
            dto.getGlucoseNormalMax().compareTo(dto.getGlucoseHighMax()) >= 0) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Logic khoảng đường huyết không hợp lệ (Cần tuân thủ: Hạ < Bình thường < Cao).");
            return "redirect:/doctor/patient-detail/" + id;
        }

        AlertThreshold entity = alertThresholdRepository.findByPatientIdAndScope(id, "PATIENT")
                .orElse(new AlertThreshold());

        entity.setPatient(patient);
        entity.setHospital(doctor.getHospital());
        entity.setScope("PATIENT");
        entity.setMetricType("COMBINED");
        entity.setCreatedByDoctorId(doctor.getId());
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        entity.setGlucoseHypoThreshold(dto.getGlucoseHypoThreshold());
        entity.setGlucoseNormalMax(dto.getGlucoseNormalMax());
        entity.setGlucoseHighMax(dto.getGlucoseHighMax());

        entity.setSystolicNormalMax(dto.getSystolicNormalMax());
        entity.setSystolicWarningMin(dto.getSystolicWarningMin());
        entity.setSystolicWarningMax(dto.getSystolicWarningMax());
        entity.setSystolicDangerMin(dto.getSystolicDangerMin());
        entity.setSystolicDangerMax(dto.getSystolicDangerMax());
        entity.setSystolicEmergencyThreshold(dto.getSystolicEmergencyThreshold());

        entity.setDiastolicNormalMax(dto.getDiastolicNormalMax());
        entity.setDiastolicWarningMin(dto.getDiastolicWarningMin());
        entity.setDiastolicWarningMax(dto.getDiastolicWarningMax());
        entity.setDiastolicDangerMin(dto.getDiastolicDangerMin());
        entity.setDiastolicDangerMax(dto.getDiastolicDangerMax());
        entity.setDiastolicEmergencyThreshold(dto.getDiastolicEmergencyThreshold());

        alertThresholdRepository.save(entity);

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã lưu ngưỡng cảnh báo riêng cho bệnh nhân.");
        return "redirect:/doctor/patient-detail/" + id;
    }
}
