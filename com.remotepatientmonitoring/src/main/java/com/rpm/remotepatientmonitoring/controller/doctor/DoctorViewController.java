package com.rpm.remotepatientmonitoring.controller.doctor;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
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

        model.addAttribute("doctor", doctor);
        model.addAttribute("patient", patient);
        model.addAttribute("allProfiles", allProfiles);
        model.addAttribute("currentRule", currentRule);
        model.addAttribute("success", success);

        return "doctor/patient-detail";
    }

    // =========================================================
    // 4. POST: Lưu Phân loại Bệnh lý + Quy tắc Dinh dưỡng
    // =========================================================
    @PostMapping("/patient-detail/{id}/update-profile")
    public String updatePatientProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam("diseaseProfileId")                          Integer    diseaseProfileId,
            @RequestParam("maxCaloriesPerDay")                         Integer    maxCaloriesPerDay,
            @RequestParam("maxCarbsG")                                 BigDecimal maxCarbsG,
            @RequestParam("maxSaltG")                                  BigDecimal maxSaltG,
            @RequestParam(value = "minFiberG",    defaultValue = "25") BigDecimal minFiberG,
            @RequestParam(value = "maxFatG",      required = false)    BigDecimal maxFatG,
            @RequestParam(value = "minProteinG",  required = false)    BigDecimal minProteinG,
            @RequestParam(value = "dailyWaterMl", defaultValue = "2000") Integer  dailyWaterMl,
            @RequestParam(value = "additionalNotes", required = false) String     additionalNotes) {

        // Xác thực bác sĩ qua Spring Security — không hardcode ID
        Integer accountId = userDetails.getAccount().getId();
        Doctor doctor = doctorRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

        // Lấy bệnh nhân
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân với ID: " + id));

        // --- B: Cập nhật phân loại bệnh lý ---
        DiseaseProfile selectedProfile = new DiseaseProfile();
        selectedProfile.setId(diseaseProfileId);
        patient.setDiseaseProfile(selectedProfile);
        patient.setStatus("TREATING");
        patientRepository.save(patient);

        // --- C: Vô hiệu hóa quy tắc dinh dưỡng cũ nếu tồn tại ---
        nutritionRuleRepository.findByPatientIdAndIsCurrent(patient.getId(), true)
                .ifPresent(old -> {
                    old.setIsCurrent(false);
                    old.setUpdatedAt(LocalDateTime.now());
                    nutritionRuleRepository.save(old);
                });

        // --- C: Tạo quy tắc dinh dưỡng mới ---
        NutritionRule newRule = new NutritionRule();
        newRule.setPatient(patient);
        newRule.setDoctor(doctor);
        newRule.setMaxCaloriesPerDay(maxCaloriesPerDay);
        newRule.setMaxCarbsG(maxCarbsG);
        newRule.setMaxSaltG(maxSaltG);
        newRule.setMinFiberG(minFiberG);
        newRule.setMaxFatG(maxFatG);
        newRule.setMinProteinG(minProteinG);
        newRule.setDailyWaterMl(dailyWaterMl);
        newRule.setAdditionalNotes(additionalNotes);
        newRule.setIsCurrent(true);
        newRule.setEffectiveFrom(LocalDate.now());
        newRule.setCreatedAt(LocalDateTime.now());
        newRule.setUpdatedAt(LocalDateTime.now());
        nutritionRuleRepository.save(newRule);

        // Redirect về trang chi tiết với flag thành công
        return "redirect:/doctor/patient-detail/" + id + "?success=true";
    }
}
