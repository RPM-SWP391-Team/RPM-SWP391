package com.rpm.remotepatientmonitoring.controller.doctor;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.doctor.TreatmentPlanWorkflowService;
import com.rpm.remotepatientmonitoring.service.RatingService;
import com.rpm.remotepatientmonitoring.dto.hospital.AlertThresholdsDTO;
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
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

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
    private final com.rpm.remotepatientmonitoring.service.patient.PatientHealthService patientHealthService;
    private final com.rpm.remotepatientmonitoring.repository.AuditTrailRepository auditTrailRepository;
    private final com.rpm.remotepatientmonitoring.service.doctor.AuditTrailService auditTrailService;

    @ModelAttribute
    public void addNotificationAttributes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            doctorRepository.findByAccountId(userDetails.getAccount().getId()).ifPresent(doctor -> {
                long unreadNotificationsCount = notificationRepository.countByDoctorIdAndIsReadFalse(doctor.getId());
                model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);
                model.addAttribute("doctor", doctor);
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
        long orangeAlertsCount = alertRepository.countUnresolvedAlertsByColor(doctor.getId(), "ORANGE");
        yellowAlertsCount += orangeAlertsCount; // Combine YELLOW and ORANGE for the dashboard view

        // 3. Lấy chỉ số đo mới nhất của từng bệnh nhân trong trang hiện tại và tính cảnh báo cao nhất
        Map<Integer, DailyHealthLog> latestLogs = new HashMap<>();
        Map<Integer, String> patientHighestAlerts = new HashMap<>();
        
        for (Patient p : patientPage.getContent()) {
            DailyHealthLog mergedLog = new DailyHealthLog();
            
            // Get latest BP
            healthLogRepository.findFirstByPatientIdAndSystolicBpIsNotNullOrderByLogTimeDesc(p.getId())
                    .ifPresent(bpLog -> {
                        mergedLog.setSystolicBp(bpLog.getSystolicBp());
                        mergedLog.setDiastolicBp(bpLog.getDiastolicBp());
                        mergedLog.setHeartRate(bpLog.getHeartRate());
                        mergedLog.setLogDate(bpLog.getLogDate());
                        mergedLog.setLogTime(bpLog.getLogTime());
                        mergedLog.setLogType(bpLog.getLogType());
                    });
            
            // Get latest Glucose
            healthLogRepository.findFirstByPatientIdAndGlucoseLevelIsNotNullOrderByLogTimeDesc(p.getId())
                    .ifPresent(glLog -> {
                        mergedLog.setGlucoseLevel(glLog.getGlucoseLevel());
                        if (mergedLog.getLogTime() == null || glLog.getLogTime().isAfter(mergedLog.getLogTime())) {
                            mergedLog.setLogDate(glLog.getLogDate());
                            mergedLog.setLogTime(glLog.getLogTime());
                            mergedLog.setLogType(glLog.getLogType());
                        }
                    });
            
            // Get latest log overall to inherit other properties if needed
            healthLogRepository.findFirstByPatientIdOrderByLogTimeDesc(p.getId())
                    .ifPresent(log -> {
                        mergedLog.setId(log.getId());
                        mergedLog.setPatient(log.getPatient());
                        mergedLog.setInputMethod(log.getInputMethod());
                        mergedLog.setPatientNotes(log.getPatientNotes());
                        mergedLog.setAlertLevel(log.getAlertLevel());
                    });
            
            if (mergedLog.getPatient() != null) {
                latestLogs.put(p.getId(), mergedLog);
            }

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
    // GET: Trang Chọn Bác Sĩ Để Chuyển Tuyến (Transfer)
    // =========================================================
    @GetMapping("/patient-detail/{id}/transfer")
    public String showTransferPatientPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "specialty", required = false) String specialty,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size,
            RedirectAttributes redirectAttributes,
            Model model) {

        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        if (patient.getDoctor() == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Chỉ Bác sĩ Phụ trách chính mới có quyền thực hiện Chuyển Bác sĩ phụ trách!");
            return "redirect:/doctor/patient-detail/" + id;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Doctor> replacementDoctors = doctorRepository.searchReplacementDoctors(
                doctor.getHospital().getId(), doctor.getId(), keyword, specialty, pageable);

        List<String> specialties = doctorRepository.findDistinctSpecialtiesByHospital(doctor.getHospital().getId());

        model.addAttribute(ATTR_DOCTOR, doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("replacementDoctors", replacementDoctors);
        model.addAttribute("specialties", specialties);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedSpecialty", specialty);

        return "doctor/transfer-patient";
    }

    // =========================================================
    // POST: Xử lý Chuyển Bác Sĩ
    // =========================================================
    @PostMapping("/patient-detail/{id}/transfer")
    public String processTransferPatient(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam("newDoctorId") Integer newDoctorId,
            @RequestParam("transferReason") String transferReason,
            RedirectAttributes redirectAttributes) {

        if (transferReason == null || transferReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Vui lòng nhập lý do bàn giao!");
            return "redirect:/doctor/patient-detail/" + id + "/transfer";
        }

        Integer accountId = userDetails.getAccount().getId();
        Doctor currentDoctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        if (patient.getDoctor() == null || !patient.getDoctor().getId().equals(currentDoctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Chỉ Bác sĩ Phụ trách chính mới có quyền bàn giao bệnh nhân này!");
            return "redirect:/doctor/patient-detail/" + id;
        }

        Doctor newDoctor = doctorRepository.findById(newDoctorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Bác sĩ mới."));

        if (!newDoctor.getHospital().getId().equals(currentDoctor.getHospital().getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Chỉ được chuyển tuyến trong cùng Bệnh viện!");
            return "redirect:/doctor/patient-detail/" + id + "/transfer";
        }

        if (newDoctor.getCurrentPatientCount() >= newDoctor.getCapacityLimit()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Bác sĩ mới đã đạt giới hạn bệnh nhân!");
            return "redirect:/doctor/patient-detail/" + id + "/transfer";
        }

        // Tạo Yêu cầu Bàn giao bệnh nhân (ChangeRequest) chờ Bác sĩ mới (Doctor B) xác nhận tiếp nhận
        ChangeRequest transferReq = ChangeRequest.builder()
                .patient(patient)
                .doctor(newDoctor) // Bác sĩ B nhận yêu cầu trong Quản lý Yêu cầu
                .requestType("TREATMENT_PLAN")
                .patientReason("[BÀN GIAO BÁC SĨ] Yêu cầu bàn giao từ Bác sĩ " + currentDoctor.getFullName() + ". Lý do: " + transferReason)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        changeRequestRepository.save(transferReq);

        // Thông báo cho bác sĩ mới (Doctor B)
        Notification docNotif = Notification.builder()
                .doctor(newDoctor)
                .patient(patient)
                .recipientType("DOCTOR")
                .recipientId(newDoctor.getId())
                .notificationType("SYSTEM")
                .channel("IN_APP")
                .status("SENT")
                .title("Yêu cầu bàn giao bệnh nhân mới")
                .content("Bác sĩ " + currentDoctor.getFullName() + " vừa gửi yêu cầu bàn giao bệnh nhân " + patient.getFullName() + " cho bạn. Vui lòng vào Quản lý Yêu cầu để duyệt tiếp nhận.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(docNotif);

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã gửi yêu cầu bàn giao bệnh nhân " + patient.getFullName() + " đến Bác sĩ " + newDoctor.getFullName() + ". Đang chờ Bác sĩ " + newDoctor.getFullName() + " duyệt tiếp nhận.");
        return "redirect:/doctor/patient-detail/" + id;
    }

    // =========================================================
    // POST: Trả bệnh nhân về Bệnh viện
    // =========================================================
    @PostMapping("/patient-detail/{id}/return-to-hospital")
    public String processReturnToHospital(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam("returnReason") String returnReason,
            RedirectAttributes redirectAttributes) {

        if (returnReason == null || returnReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Vui lòng nhập lý do từ chối/trả về!");
            return "redirect:/doctor/patient-detail/" + id;
        }

        Integer accountId = userDetails.getAccount().getId();
        Doctor currentDoctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        if (patient.getDoctor() == null || !patient.getDoctor().getId().equals(currentDoctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Bạn không có quyền thao tác trên bệnh nhân này!");
            return REDIRECT_DASHBOARD;
        }

        // Cập nhật số lượng bệnh nhân
        currentDoctor.setCurrentPatientCount(Math.max(0, currentDoctor.getCurrentPatientCount() - 1));
        doctorRepository.save(currentDoctor);

        // Trả về viện
        patient.setDoctor(null);
        patient.setStatus("NEW");
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        // Lưu Audit Trail
        String auditDetail = String.format("Trả bệnh nhân %s về Bệnh viện. Lý do: %s",
                patient.getFullName(), returnReason);
        auditTrailService.logAction(
                "DOCTOR",
                currentDoctor.getId(),
                "RETURN_TO_HOSPITAL",
                "patients", // targetTable
                patient.getId(),
                currentDoctor.getId(),
                null,
                auditDetail); // notes

        // Thông báo cho bệnh nhân
        Notification patNotif = Notification.builder()
                .patient(patient)
                .recipientType("PATIENT")
                .recipientId(patient.getId())
                .notificationType("SYSTEM")
                .channel("IN_APP")
                .status("SENT")
                .title("Thông báo về Bác sĩ phụ trách")
                .content("Bác sĩ phụ trách của bạn đã tạm thời ngừng tiếp nhận. Hệ thống sẽ sớm phân công bác sĩ mới cho bạn.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(patNotif);

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã trả bệnh nhân " + patient.getFullName() + " về Viện thành công.");
        return REDIRECT_DASHBOARD;
    }

    // =========================================================
    // 3. GET: Xem & Cấu hình hồ sơ chi tiết bệnh nhân
    // =========================================================
    @GetMapping("/patient-detail/{id}")
    public String showPatientDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "planPage", defaultValue = "0") int planPage,
            @RequestParam(value = "planSize", defaultValue = "5") int planSize,
            @RequestParam(value = "planKeyword", required = false) String planKeyword,
            Model model) {

        // Lấy đúng bác sĩ từ session Spring Security — không hardcode ID
        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

        // Lấy bệnh nhân theo path ID
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        // Kiểm tra bảo mật: Bệnh nhân phải thuộc quyền quản lý của bác sĩ HOẶC bác sĩ có lịch hẹn khám với bệnh nhân này
        boolean isPrimaryDoctor = patient.getDoctor() != null && patient.getDoctor().getId().equals(doctor.getId());
        boolean hasAppointment = appointmentRepository.existsByPatientIdAndDoctorId(patient.getId(), doctor.getId());

        if (!isPrimaryDoctor && !hasAppointment) {
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
        
        Pageable planPageable = PageRequest.of(planPage, planSize);
        Page<TreatmentPlan> planHistoryPage = treatmentPlanRepository.searchPlanHistory(patient.getId(), planKeyword, null, null, planPageable);

        model.addAttribute(ATTR_DOCTOR, doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("isPrimaryDoctor", isPrimaryDoctor);
        model.addAttribute("allProfiles", allProfiles);
        model.addAttribute("currentRule", currentRule);
        model.addAttribute("currentPlan", currentPlan);
        model.addAttribute("currentMeds", currentMeds);
        model.addAttribute("planHistoryPage", planHistoryPage);
        model.addAttribute("planKeyword", planKeyword != null ? planKeyword : "");
        model.addAttribute("planPage", planPage);
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
            @RequestParam(value = "heightCm", required = false) BigDecimal heightCm,
            @RequestParam(value = "bmi", required = false) BigDecimal bmi,
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
            @RequestParam(value = "medScheduledTimes", required = false) List<String> medScheduledTimes,
            RedirectAttributes redirectAttributes
    ) {

        // Xác thực bác sĩ qua Spring Security
        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException(MSG_DOCTOR_NOT_FOUND));

        // Lấy bệnh nhân
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        // Kiểm tra bảo mật: Chỉ Bác sĩ phụ trách chính mới được quyền sửa Phác đồ điều trị dài hạn!
        boolean isPrimaryDoctor = patient.getDoctor() != null && patient.getDoctor().getId().equals(doctor.getId());
        if (!isPrimaryDoctor) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Bạn không phải Bác sĩ phụ trách chính của Bệnh nhân này nên không có quyền thay đổi Phác đồ điều trị dài hạn!");
            return "redirect:/doctor/patient-detail/" + id;
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
                heightCm,
                bmi,
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
                                     @RequestParam(value = "patientName", required = false) String patientName,
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
                changeRequestPage = changeRequestRepository.findHistoryByDate(doctor.getId(), patientName, filterStart, filterEnd, pageable);
            } else {
                changeRequestPage = changeRequestRepository.findHistory(doctor.getId(), patientName, pageable);
            }
        } else {
            if (filterStart != null && filterEnd != null) {
                changeRequestPage = changeRequestRepository.findPendingByDate(doctor.getId(), patientName, filterStart, filterEnd, pageable);
            } else {
                changeRequestPage = changeRequestRepository.findPending(doctor.getId(), patientName, pageable);
            }
        }

        long pendingCount = changeRequestRepository.countByDoctorIdAndStatus(doctor.getId(), "PENDING");

        model.addAttribute(ATTR_DOCTOR, doctor);
        model.addAttribute("changeRequestPage", changeRequestPage);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("activeTab", tab);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("patientName", patientName);

        return "doctor/change-requests";
    }

    @PostMapping("/change-requests/{id}/process")
    public String processChangeRequest(
            @PathVariable("id") Integer id,
            @RequestParam("action") String action,
            @RequestParam(value = "doctorResponse", required = false) String doctorResponse,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return REDIRECT_LOGIN;
        }

        ChangeRequest request = changeRequestRepository.findById(id).orElse(null);
        if (request == null || request.getDoctor() == null || !request.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Bạn không có quyền xử lý yêu cầu này.");
            return REDIRECT_CHANGE_REQUESTS;
        }

        boolean isTransfer = (request.getPatientReason() != null && request.getPatientReason().startsWith("[BÀN GIAO BÁC SĨ]")) || "TRANSFER".equalsIgnoreCase(request.getRequestType());

        if ("APPROVE".equalsIgnoreCase(action)) {
            // Nếu đây là Yêu cầu bàn giao Bác sĩ (TRANSFER)
            if (isTransfer) {
                Patient patient = request.getPatient();
                Doctor oldDoctor = patient.getDoctor();
                Doctor newDoctor = doctor; // Bác sĩ B nhận bàn giao

                if (newDoctor.getCurrentPatientCount() >= newDoctor.getCapacityLimit()) {
                    redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Bạn đã đạt giới hạn sức chứa bệnh nhân, không thể tiếp nhận thêm!");
                    return REDIRECT_CHANGE_REQUESTS;
                }

                if (oldDoctor != null && oldDoctor.getCurrentPatientCount() > 0) {
                    oldDoctor.setCurrentPatientCount(oldDoctor.getCurrentPatientCount() - 1);
                    doctorRepository.save(oldDoctor);
                }
                newDoctor.setCurrentPatientCount(newDoctor.getCurrentPatientCount() + 1);
                doctorRepository.save(newDoctor);

                patient.setDoctor(newDoctor);
                patient.setUpdatedAt(LocalDateTime.now());
                patientRepository.save(patient);

                // Ghi Audit Trail
                String auditDetail = String.format("Bác sĩ %s đã đồng ý tiếp nhận bàn giao bệnh nhân %s từ Bác sĩ %s.",
                        newDoctor.getFullName(), patient.getFullName(), oldDoctor != null ? oldDoctor.getFullName() : "N/A");
                auditTrailService.logAction(
                        "DOCTOR",
                        newDoctor.getId(),
                        "TRANSFER_PATIENT",
                        "patients",
                        patient.getId(),
                        oldDoctor != null ? oldDoctor.getId() : null,
                        newDoctor.getId(),
                        auditDetail);

                // Thông báo cho Bác sĩ cũ và Bệnh nhân
                if (oldDoctor != null) {
                    Notification oldDocNotif = Notification.builder()
                            .doctor(oldDoctor)
                            .patient(patient)
                            .recipientType("DOCTOR")
                            .recipientId(oldDoctor.getId())
                            .title("Duyệt bàn giao bệnh nhân")
                            .content("Bác sĩ " + newDoctor.getFullName() + " đã đồng ý tiếp nhận bàn giao bệnh nhân " + patient.getFullName() + ".")
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(oldDocNotif);
                }

                Notification patNotif = Notification.builder()
                        .patient(patient)
                        .recipientType("PATIENT")
                        .recipientId(patient.getId())
                        .title("Bác sĩ phụ trách mới")
                        .content("Bác sĩ " + newDoctor.getFullName() + " đã chính thức tiếp nhận phụ trách điều trị cho bạn.")
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                notificationRepository.save(patNotif);
            }

            request.setStatus("APPROVED");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            request.setStatus("REJECTED");

            if (isTransfer) {
                Doctor oldDoctor = request.getPatient().getDoctor();
                if (oldDoctor != null) {
                    Notification oldDocNotif = Notification.builder()
                            .doctor(oldDoctor)
                            .patient(request.getPatient())
                            .recipientType("DOCTOR")
                            .recipientId(oldDoctor.getId())
                            .title("Từ chối tiếp nhận bàn giao")
                            .content("Bác sĩ " + doctor.getFullName() + " đã từ chối tiếp nhận bàn giao bệnh nhân " + request.getPatient().getFullName() + (doctorResponse != null && !doctorResponse.trim().isEmpty() ? ". Lý do: " + doctorResponse : "."))
                            .isRead(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(oldDocNotif);
                }
            }
        }
        request.setDoctorResponse(doctorResponse);
        request.setProcessedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        changeRequestRepository.save(request);

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã xử lý yêu cầu thành công!");
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
                                   @RequestParam(value = "patientName", required = false) String patientName,
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
                appointmentPage = appointmentRepository.findHistoryByDate(doctor.getId(), patientName, filterStart, filterEnd, now, pageable);
            } else {
                appointmentPage = appointmentRepository.findHistory(doctor.getId(), patientName, now, pageable);
            }
        } else {
            if (filterStart != null && filterEnd != null) {
                appointmentPage = appointmentRepository.findUpcomingByDate(doctor.getId(), patientName, filterStart, filterEnd, now, pageable);
            } else {
                appointmentPage = appointmentRepository.findUpcoming(doctor.getId(), patientName, now, pageable);
            }
        }

        long upcomingCount = appointmentRepository.countUpcoming(doctor.getId(), patientName, now);

        model.addAttribute(ATTR_DOCTOR, doctor);
        model.addAttribute("patients", patientRepository.findByDoctorIdAndIsActiveTrue(doctor.getId()));
        
        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("activeTab", tab);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("patientName", patientName);

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
        if (patient == null || patient.getDoctor() == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Không thể lên lịch cho bệnh nhân này.");
            return REDIRECT_APPOINTMENTS;
        }

        try {
            if (apptTime.isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Thời gian hẹn phải ở trong tương lai.");
                return REDIRECT_APPOINTMENTS;
            }

            int hour = apptTime.getHour();
            if (hour < 8 || hour >= 17) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Giờ hẹn phải nằm trong giờ làm việc (Từ 08:00 sáng đến 17:00 chiều).");
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

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable("id") Integer id,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestParam(value = "systolicBp", required = false) Integer systolicBp,
                                      @RequestParam(value = "diastolicBp", required = false) Integer diastolicBp,
                                      @RequestParam(value = "heartRate", required = false) Integer heartRate,
                                      @RequestParam(value = "glucoseLevel", required = false) BigDecimal glucoseLevel,
                                      @RequestParam(value = "doctorNote", required = false) String doctorNote,
                                      RedirectAttributes redirectAttributes) {
        Doctor doctor = doctorRepository.findByAccountId(userDetails.getAccount().getId()).orElse(null);
        if (doctor == null) {
            return REDIRECT_LOGIN;
        }

        Appointment appt = appointmentRepository.findById(id).orElse(null);
        if (appt == null) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, MSG_APPOINTMENT_NOT_FOUND);
            return REDIRECT_APPOINTMENTS;
        }

        if (!appt.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Bạn không có quyền hoàn thành lịch hẹn này.");
            return REDIRECT_APPOINTMENTS;
        }

        appt.setStatus("COMPLETED");
        appt.setCompletedAt(LocalDateTime.now());
        if (doctorNote != null && !doctorNote.trim().isEmpty()) {
            appt.setDoctorNote(doctorNote.trim());
        }
        appt.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appt);

        Patient patient = appt.getPatient();

        // Lưu nhật ký sức khỏe từ kết quả khám lâm sàng trực tiếp của Bác sĩ (nếu có nhập chỉ số)
        if (patient != null && (systolicBp != null || diastolicBp != null || heartRate != null || glucoseLevel != null)) {
            DailyHealthLog healthLog = DailyHealthLog.builder()
                    .patient(patient)
                    .logDate(LocalDate.now())
                    .logTime(LocalDateTime.now())
                    .logType("RANDOM")
                    .systolicBp(systolicBp)
                    .diastolicBp(diastolicBp)
                    .heartRate(heartRate)
                    .glucoseLevel(glucoseLevel)
                    .inputMethod("MANUAL")
                    .patientNotes("Chỉ số đo lâm sàng trực tiếp tại viện bởi Bác sĩ " + doctor.getFullName())
                    .isAlertProcessed(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            healthLogRepository.save(healthLog);
            
            com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest alertReq = new com.rpm.remotepatientmonitoring.dto.patient.HealthLogRequest();
            alertReq.setPatientId(patient.getId());
            alertReq.setLogType("RANDOM");
            alertReq.setInputMethod("MANUAL");
            alertReq.setSystolicBp(systolicBp);
            alertReq.setDiastolicBp(diastolicBp);
            alertReq.setHeartRate(heartRate);
            alertReq.setGlucoseLevel(glucoseLevel);
            alertReq.setPatientNotes("Chỉ số đo lâm sàng trực tiếp tại viện bởi Bác sĩ " + doctor.getFullName());
            patientHealthService.evaluateAndGenerateAlerts(alertReq);
        }

        // Gửi thông báo cho bệnh nhân
        Notification notif = Notification.builder()
                .patient(patient)
                .doctor(doctor)
                .recipientType("PATIENT")
                .title("Lịch khám đã hoàn thành")
                .content("Bác sĩ " + doctor.getFullName() + " đã hoàn thành buổi khám cho bạn.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notif);

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã hoàn thành buổi khám cho bệnh nhân " + (patient != null ? patient.getFullName() : "") + " thành công!");
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
        if (alert == null || alert.getPatient() == null || alert.getPatient().getDoctor() == null || !alert.getPatient().getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Bạn không có quyền xử lý cảnh báo này.");
            return REDIRECT_DASHBOARD;
        }

        alert.setIsResolved(true);
        alert.setResolvedAt(java.time.LocalDateTime.now());
        alert.setResolvedByDoctor(doctor);
        alert.setResolutionNotes(resolutionNotes);
        alertRepository.save(alert);

        // Gửi thông báo trực tiếp cho bệnh nhân
        if (alert.getPatient() != null) {
            Notification notif = Notification.builder()
                    .patient(alert.getPatient())
                    .doctor(doctor)
                    .recipientType("PATIENT")
                    .recipientId(alert.getPatient().getId())
                    .notificationType("ALERT_RESOLVED")
                    .channel("IN_APP")
                    .status("SENT")
                    .title("Hướng dẫn xử lý cảnh báo y tế từ Bác sĩ " + doctor.getFullName())
                    .content("Bác sĩ " + doctor.getFullName() + " đã xử lý cảnh báo y tế của bạn với ghi chú hướng dẫn: " + resolutionNotes)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        }

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

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã xử lý cảnh báo y tế.");
        return "redirect:/doctor/patient-detail/" + alert.getPatient().getId() + "?success=alert-resolved";
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

        if (notif.getTitle() != null && notif.getTitle().toLowerCase().contains("bàn giao")) {
            return REDIRECT_CHANGE_REQUESTS;
        }

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

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(
            @PathVariable("id") Integer id,
            @RequestParam(value = "cancelReason", required = false) String cancelReason,
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

        if ("COMPLETED".equals(appt.getStatus()) || "CANCELLED".equals(appt.getStatus())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Lịch hẹn này không thể hủy.");
            return REDIRECT_APPOINTMENTS;
        }

        appt.setStatus("CANCELLED");
        if (cancelReason != null && !cancelReason.trim().isEmpty()) {
            appt.setDoctorNote("Hủy lịch: " + cancelReason.trim());
        }
        appt.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appt);

        if (appt.getPatient() != null) {
            Notification notif = Notification.builder()
                    .patient(appt.getPatient())
                    .doctor(doctor)
                    .recipientType("PATIENT")
                    .title("Lịch khám đã bị hủy bởi Bác sĩ")
                    .content("Bác sĩ " + doctor.getFullName() + " đã hủy lịch khám vào "
                            + appt.getAppointmentTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            + (cancelReason != null && !cancelReason.trim().isEmpty() ? ". Lý do: " + cancelReason.trim() : ""))
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notif);
        }

        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MSG, "Đã hủy lịch hẹn thành công.");
        return REDIRECT_APPOINTMENTS;
    }

    // =========================================================
    // GET: Trang Cấu hình Ngưỡng Cảnh Báo Riêng Cho Bệnh Nhân và Lịch sử
    // =========================================================
    @GetMapping("/patient-detail/{id}/thresholds")
    public String showPatientThresholds(
            @PathVariable Integer id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "date", required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate searchDate,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId).orElse(null);
        if (doctor == null) return REDIRECT_LOGIN;

        Patient patient = patientRepository.findById(id).orElse(null);
        if (patient == null || patient.getDoctor() == null || !patient.getDoctor().getId().equals(doctor.getId())) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Không tìm thấy bệnh nhân hoặc bạn không có quyền.");
            return REDIRECT_DASHBOARD;
        }

        model.addAttribute("patient", patient);
        if (patient.getDateOfBirth() != null) {
            model.addAttribute("patientAge", java.time.Period.between(patient.getDateOfBirth(), java.time.LocalDate.now()).getYears());
        }

        // Fetch current threshold
        AlertThreshold threshold = alertThresholdRepository.findByPatientIdAndScope(patient.getId(), "PATIENT")
                .orElse(null);
        
        boolean isPersonalized = true;

        if (threshold == null) {
            isPersonalized = false;
            // Get hospital default
            if (doctor.getHospital() != null) {
                threshold = alertThresholdRepository.findByHospitalIdAndScope(doctor.getHospital().getId(), "HOSPITAL")
                        .orElse(new AlertThreshold());
            } else {
                threshold = new AlertThreshold();
            }
        }

        if (!model.containsAttribute("threshold")) {
            AlertThresholdsDTO thresholdDto = new AlertThresholdsDTO();
            thresholdDto.setGlucoseHypoThreshold(threshold.getGlucoseHypoThreshold() != null ? threshold.getGlucoseHypoThreshold() : java.math.BigDecimal.valueOf(4.0));
            thresholdDto.setGlucoseNormalMax(threshold.getGlucoseNormalMax() != null ? threshold.getGlucoseNormalMax() : java.math.BigDecimal.valueOf(7.8));
            thresholdDto.setGlucoseHighMax(threshold.getGlucoseHighMax() != null ? threshold.getGlucoseHighMax() : java.math.BigDecimal.valueOf(11.1));
            
            thresholdDto.setSystolicNormalMax(threshold.getSystolicNormalMax() != null ? threshold.getSystolicNormalMax() : 120);
            thresholdDto.setSystolicWarningMin(threshold.getSystolicWarningMin() != null ? threshold.getSystolicWarningMin() : 121);
            thresholdDto.setSystolicWarningMax(threshold.getSystolicWarningMax() != null ? threshold.getSystolicWarningMax() : 139);
            thresholdDto.setSystolicDangerMin(threshold.getSystolicDangerMin() != null ? threshold.getSystolicDangerMin() : 140);
            thresholdDto.setSystolicDangerMax(threshold.getSystolicDangerMax() != null ? threshold.getSystolicDangerMax() : 179);
            thresholdDto.setSystolicEmergencyThreshold(threshold.getSystolicEmergencyThreshold() != null ? threshold.getSystolicEmergencyThreshold() : 180);
            
            thresholdDto.setDiastolicNormalMax(threshold.getDiastolicNormalMax() != null ? threshold.getDiastolicNormalMax() : 80);
            thresholdDto.setDiastolicWarningMin(threshold.getDiastolicWarningMin() != null ? threshold.getDiastolicWarningMin() : 81);
            thresholdDto.setDiastolicWarningMax(threshold.getDiastolicWarningMax() != null ? threshold.getDiastolicWarningMax() : 89);
            thresholdDto.setDiastolicDangerMin(threshold.getDiastolicDangerMin() != null ? threshold.getDiastolicDangerMin() : 90);
            thresholdDto.setDiastolicDangerMax(threshold.getDiastolicDangerMax() != null ? threshold.getDiastolicDangerMax() : 119);
            thresholdDto.setDiastolicEmergencyThreshold(threshold.getDiastolicEmergencyThreshold() != null ? threshold.getDiastolicEmergencyThreshold() : 120);

            model.addAttribute("threshold", thresholdDto);
        }
        model.addAttribute("isPersonalized", isPersonalized);
        
        // Fetch audit trails (history)
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<com.rpm.remotepatientmonitoring.model.AuditTrail> historyPage;
        
        // Target record ID is the threshold ID if it's personalized, otherwise we might not have a patient-specific threshold yet.
        // Actually we only show history for the PATIENT scope threshold.
        AlertThreshold patientThreshold = alertThresholdRepository.findByPatientIdAndScope(patient.getId(), "PATIENT").orElse(null);
        
        if (patientThreshold != null) {
            if (searchDate != null) {
                java.time.LocalDateTime startOfDay = searchDate.atStartOfDay();
                java.time.LocalDateTime endOfDay = searchDate.atTime(23, 59, 59, 999999999);
                historyPage = auditTrailRepository.findByTargetTableAndTargetRecordIdAndActionAndCreatedAtBetweenOrderByCreatedAtDesc(
                        "AlertThreshold", patientThreshold.getId(), "UPDATE_PATIENT_THRESHOLD", startOfDay, endOfDay, pageable);
                model.addAttribute("searchDate", searchDate);
            } else {
                historyPage = auditTrailRepository.findByTargetTableAndTargetRecordIdAndActionOrderByCreatedAtDesc(
                        "AlertThreshold", patientThreshold.getId(), "UPDATE_PATIENT_THRESHOLD", pageable);
            }
        } else {
            historyPage = org.springframework.data.domain.Page.empty(pageable);
        }
        
        model.addAttribute("historyPage", historyPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());

        return "doctor/patient-thresholds";
    }

    // =========================================================
    // POST: Lưu Cấu hình Ngưỡng Cảnh Báo Riêng Cho Bệnh Nhân
    // =========================================================
    @PostMapping("/patient-detail/{id}/update-thresholds")
    public String updatePatientThresholds(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("threshold") AlertThresholdsDTO dto,
            BindingResult bindingResult,
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

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, "Vui lòng điền đầy đủ và chính xác các chỉ số (không được bỏ trống).");
            redirectAttributes.addFlashAttribute("threshold", dto);
            return "redirect:/doctor/patient-detail/" + id + "/thresholds";
        }

        // Validate logic khoảng cảnh báo Tâm thu
        String errorMsg = null;
        if (dto.getSystolicNormalMax() >= dto.getSystolicWarningMin() ||
            dto.getSystolicWarningMin() > dto.getSystolicWarningMax() ||
            dto.getSystolicWarningMax() >= dto.getSystolicDangerMin() ||
            dto.getSystolicDangerMin() > dto.getSystolicDangerMax() ||
            dto.getSystolicDangerMax() >= dto.getSystolicEmergencyThreshold()) {
            errorMsg = "Logic khoảng huyết áp Tâm thu không hợp lệ (Cần tuân thủ: Bình thường < Cảnh báo < Nguy hiểm < Cấp cứu).";
        }
        // Validate logic khoảng cảnh báo Tâm trương
        else if (dto.getDiastolicNormalMax() >= dto.getDiastolicWarningMin() ||
            dto.getDiastolicWarningMin() > dto.getDiastolicWarningMax() ||
            dto.getDiastolicWarningMax() >= dto.getDiastolicDangerMin() ||
            dto.getDiastolicDangerMin() > dto.getDiastolicDangerMax() ||
            dto.getDiastolicDangerMax() >= dto.getDiastolicEmergencyThreshold()) {
            errorMsg = "Logic khoảng huyết áp Tâm trương không hợp lệ (Cần tuân thủ: Bình thường < Cảnh báo < Nguy hiểm < Cấp cứu).";
        }
        // Validate logic khoảng đường huyết
        else if (dto.getGlucoseHypoThreshold().compareTo(dto.getGlucoseNormalMax()) >= 0 ||
            dto.getGlucoseNormalMax().compareTo(dto.getGlucoseHighMax()) >= 0) {
            errorMsg = "Logic khoảng đường huyết không hợp lệ (Cần tuân thủ: Hạ < Bình thường < Cao).";
        }

        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR_MSG, errorMsg);
            redirectAttributes.addFlashAttribute("threshold", dto);
            return "redirect:/doctor/patient-detail/" + id + "/thresholds";
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
        return "redirect:/doctor/patient-detail/" + id + "/thresholds";
    }
}