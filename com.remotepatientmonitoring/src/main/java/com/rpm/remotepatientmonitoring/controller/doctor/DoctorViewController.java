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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
            patientPage = patientRepository.findByDoctorId(doctor.getId(), pageable);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("patientPage", patientPage);
        model.addAttribute("keyword", keyword);

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

        model.addAttribute("doctor", doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("allProfiles", allProfiles);
        model.addAttribute("currentRule", currentRule);
        model.addAttribute("currentPlan", currentPlan);
        model.addAttribute("currentMeds", currentMeds);
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
}
