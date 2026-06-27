package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.model.AlertThreshold;
import com.rpm.remotepatientmonitoring.model.EmergencyGuide;
import com.rpm.remotepatientmonitoring.service.HospitalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hospital")
public class HospitalConfigController {

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @Autowired
    private HospitalConfigService configService;

    @GetMapping("/config")
    public String getConfigPage(Model model) {
        AlertThreshold thresholdEntity = configService.getGlobalThreshold(HARDCODED_HOSPITAL_ID);
        List<EmergencyGuide> guides = configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID);

        // Chuyển dữ liệu từ Entity sang DTO sạch
        com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO thresholdDTO =
                com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO.builder()
                        .id(thresholdEntity.getId())
                        .glucoseNormalMin(thresholdEntity.getGlucoseNormalMin())
                        .glucoseNormalMax(thresholdEntity.getGlucoseNormalMax())
                        .glucoseWarningMin(thresholdEntity.getGlucoseWarningMin())
                        .glucoseWarningMax(thresholdEntity.getGlucoseWarningMax())
                        .glucoseTreatingMin(thresholdEntity.getGlucoseTreatingMin())
                        .glucoseTreatingMax(thresholdEntity.getGlucoseTreatingMax())
                        .glucoseDangerThreshold(thresholdEntity.getGlucoseDangerThreshold())
                        .systolicNormalMax(thresholdEntity.getSystolicNormalMax())
                        .systolicPrehypertensionMin(thresholdEntity.getSystolicPrehypertensionMin())
                        .systolicPrehypertensionMax(thresholdEntity.getSystolicPrehypertensionMax())
                        .systolicHypertensionMin(thresholdEntity.getSystolicHypertensionMin())
                        .systolicHypertensionMax(thresholdEntity.getSystolicHypertensionMax())
                        .systolicDangerThreshold(thresholdEntity.getSystolicDangerThreshold())
                        .systolicEmergencyThreshold(thresholdEntity.getSystolicEmergencyThreshold())
                        .diastolicNormalMax(thresholdEntity.getDiastolicNormalMax())
                        .diastolicHypertensionMin(thresholdEntity.getDiastolicHypertensionMin())
                        .diastolicHypertensionMax(thresholdEntity.getDiastolicHypertensionMax())
                        .diastolicDangerThreshold(thresholdEntity.getDiastolicDangerThreshold())
                        .diastolicEmergencyThreshold(thresholdEntity.getDiastolicEmergencyThreshold())
                        .build();

        model.addAttribute("threshold", thresholdDTO); // Truyền DTO thay vì Entity
        model.addAttribute("guides", guides);
        if (!model.containsAttribute("newGuide")) {
            model.addAttribute("newGuide", new com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO());
        }
        return "hospital/config";
    }

    @PostMapping("/config/thresholds/update")
    public String updateThresholds(@jakarta.validation.Valid @ModelAttribute("threshold") com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO thresholdDTO,
                                   org.springframework.validation.BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        // ============================================================
        // TẦNG 0: BẮT LỖI VALIDATION TỰ ĐỘNG TỪ DTO (Hết lo null ngầm hay lỗi ép kiểu)
        // ============================================================
        if (bindingResult.hasErrors()) {
            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", new com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO());
            model.addAttribute("errorMessage", "Lỗi định dạng: Ô nhập liệu chứa ký tự chữ hoặc bị bỏ trống. Vui lòng kiểm tra lại.");
            return "hospital/config";
        }

        // ============================================================
        // TẦNG 1: KIỂM TRA MỐC CỐ ĐỊNH THEO TÀI LIỆU - ĐƯỜNG HUYẾT
        // ============================================================
        if (thresholdDTO.getGlucoseNormalMin() != null && thresholdDTO.getGlucoseNormalMin().compareTo(java.math.BigDecimal.valueOf(70)) < 0) {
            bindingResult.rejectValue("glucoseNormalMin", "error.glucoseNormalMin", "Đường huyết bình thường tối thiểu phải lớn hơn hoặc bằng 70 mg/dL.");
        }
        if (thresholdDTO.getGlucoseNormalMax() != null && thresholdDTO.getGlucoseNormalMax().compareTo(java.math.BigDecimal.valueOf(99)) > 0) {
            bindingResult.rejectValue("glucoseNormalMax", "error.glucoseNormalMax", "Đường huyết bình thường tối đa chỉ được nhỏ hơn hoặc bằng 99 mg/dL.");
        }
        if (thresholdDTO.getGlucoseWarningMin() != null && thresholdDTO.getGlucoseWarningMin().compareTo(java.math.BigDecimal.valueOf(100)) < 0) {
            bindingResult.rejectValue("glucoseWarningMin", "error.glucoseWarningMin", "Tiền tiểu đường tối thiểu phải lớn hơn hoặc bằng 100 mg/dL.");
        }
        if (thresholdDTO.getGlucoseWarningMax() != null && thresholdDTO.getGlucoseWarningMax().compareTo(java.math.BigDecimal.valueOf(125)) > 0) {
            bindingResult.rejectValue("glucoseWarningMax", "error.glucoseWarningMax", "Tiền tiểu đường tối đa chỉ được nhỏ hơn hoặc bằng 125 mg/dL.");
        }
        if (thresholdDTO.getGlucoseTreatingMin() != null && thresholdDTO.getGlucoseTreatingMin().compareTo(java.math.BigDecimal.valueOf(80)) < 0) {
            bindingResult.rejectValue("glucoseTreatingMin", "error.glucoseTreatingMin", "Trong điều trị tối thiểu phải lớn hơn hoặc bằng 80 mg/dL.");
        }
        if (thresholdDTO.getGlucoseTreatingMax() != null && thresholdDTO.getGlucoseTreatingMax().compareTo(java.math.BigDecimal.valueOf(130)) > 0) {
            bindingResult.rejectValue("glucoseTreatingMax", "error.glucoseTreatingMax", "Trong điều trị tối đa chỉ được nhỏ hơn hoặc bằng 130 mg/dL.");
        }
        if (thresholdDTO.getGlucoseDangerThreshold() != null && thresholdDTO.getGlucoseDangerThreshold().compareTo(java.math.BigDecimal.valueOf(130)) <= 0) {
            bindingResult.rejectValue("glucoseDangerThreshold", "error.glucoseDangerThreshold", "Ngưỡng nguy hiểm cấp báo bắt buộc phải lớn hơn 130 mg/dL.");
        }

        // ============================================================
        // TẦNG 2: KIỂM TRA MỐC CỐ ĐỊNH THEO TÀI LIỆU - HUYẾT ÁP TÂM THU
        // ============================================================
        if (thresholdDTO.getSystolicNormalMax() != null && thresholdDTO.getSystolicNormalMax() > 120) {
            bindingResult.rejectValue("systolicNormalMax", "error.systolicNormalMax", "Huyết áp tâm thu bình thường phải nhỏ hơn hoặc bằng mốc 120 mmHg.");
        }
        if (thresholdDTO.getSystolicPrehypertensionMin() != null && (thresholdDTO.getSystolicPrehypertensionMin() < 120 || thresholdDTO.getSystolicPrehypertensionMin() > 129)) {
            bindingResult.rejectValue("systolicPrehypertensionMin", "error.systolicPrehypertensionMin", "Tiền tăng huyết áp tâm thu phải nằm trong khoảng từ 120 đến 129 mmHg.");
        }
        if (thresholdDTO.getSystolicHypertensionMin() != null && (thresholdDTO.getSystolicHypertensionMin() < 130 || thresholdDTO.getSystolicHypertensionMin() > 139)) {
            bindingResult.rejectValue("systolicHypertensionMin", "error.systolicHypertensionMin", "Tăng huyết áp tâm thu phải nằm trong khoảng từ 130 đến 139 mmHg.");
        }
        if (thresholdDTO.getSystolicDangerThreshold() != null && thresholdDTO.getSystolicDangerThreshold() < 140) {
            bindingResult.rejectValue("systolicDangerThreshold", "error.systolicDangerThreshold", "Ngưỡng nguy hiểm tâm thu phải lớn hơn hoặc bằng 140 mmHg.");
        }
        if (thresholdDTO.getSystolicEmergencyThreshold() != null && thresholdDTO.getSystolicEmergencyThreshold() < 180) {
            bindingResult.rejectValue("systolicEmergencyThreshold", "error.systolicEmergencyThreshold", "Ngưỡng cấp cứu tâm thu phải lớn hơn hoặc bằng 180 mmHg.");
        }

        // ============================================================
        // TẦNG 3: KIỂM TRA MỐC CỐ ĐỊNH THEO TÀI LIỆU - HUYẾT ÁP TÂM TRƯƠNG
        // ============================================================
        if (thresholdDTO.getDiastolicNormalMax() != null && thresholdDTO.getDiastolicNormalMax() > 80) {
            bindingResult.rejectValue("diastolicNormalMax", "error.diastolicNormalMax", "Huyết áp tâm trương bình thường phải nhỏ hơn hoặc bằng mốc 80 mmHg.");
        }
        if (thresholdDTO.getDiastolicHypertensionMin() != null && (thresholdDTO.getDiastolicHypertensionMin() < 80 || thresholdDTO.getDiastolicHypertensionMin() > 89)) {
            bindingResult.rejectValue("diastolicHypertensionMin", "error.diastolicHypertensionMin", "Tăng huyết áp tâm trương phải nằm trong khoảng từ 80 đến 89 mmHg.");
        }
        if (thresholdDTO.getDiastolicDangerThreshold() != null && thresholdDTO.getDiastolicDangerThreshold() < 90) {
            bindingResult.rejectValue("diastolicDangerThreshold", "error.diastolicDangerThreshold", "Ngưỡng nguy hiểm tâm trương phải lớn hơn hoặc bằng 90 mmHg.");
        }
        if (thresholdDTO.getDiastolicEmergencyThreshold() != null && thresholdDTO.getDiastolicEmergencyThreshold() < 120) {
            bindingResult.rejectValue("diastolicEmergencyThreshold", "error.diastolicEmergencyThreshold", "Ngưỡng cấp cứu tâm trương phải lớn hơn hoặc bằng 120 mmHg.");
        }

        // PHÁT HIỆN VI PHẠM MỐC TÀI LIỆU LÂM SÀNG
        if (bindingResult.hasErrors()) {
            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", new com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO());
            model.addAttribute("errorMessage", "Lỗi nghiệp vụ tài liệu: Chỉ số nhập vào vi phạm mốc quy định lâm sàng toàn viện.");
            return "hospital/config";
        }

        try {
            // MAPPING DỮ LIỆU SẠCH TỪ DTO VỀ ENTITY TRƯỚC KHI CHUYỂN CHO SERVICE LƯU XUỐNG DB
            AlertThreshold finalEntity = AlertThreshold.builder()
                    .glucoseNormalMin(thresholdDTO.getGlucoseNormalMin())
                    .glucoseNormalMax(thresholdDTO.getGlucoseNormalMax())
                    .glucoseWarningMin(thresholdDTO.getGlucoseWarningMin())
                    .glucoseWarningMax(thresholdDTO.getGlucoseWarningMax())
                    .glucoseTreatingMin(thresholdDTO.getGlucoseTreatingMin())
                    .glucoseTreatingMax(thresholdDTO.getGlucoseTreatingMax())
                    .glucoseDangerThreshold(thresholdDTO.getGlucoseDangerThreshold())
                    .systolicNormalMax(thresholdDTO.getSystolicNormalMax())
                    .systolicPrehypertensionMin(thresholdDTO.getSystolicPrehypertensionMin())
                    .systolicPrehypertensionMax(thresholdDTO.getSystolicPrehypertensionMax())
                    .systolicHypertensionMin(thresholdDTO.getSystolicHypertensionMin())
                    .systolicHypertensionMax(thresholdDTO.getSystolicHypertensionMax())
                    .systolicDangerThreshold(thresholdDTO.getSystolicDangerThreshold())
                    .systolicEmergencyThreshold(thresholdDTO.getSystolicEmergencyThreshold())
                    .diastolicNormalMax(thresholdDTO.getDiastolicNormalMax())
                    .diastolicHypertensionMin(thresholdDTO.getDiastolicHypertensionMin())
                    .diastolicHypertensionMax(thresholdDTO.getDiastolicHypertensionMax())
                    .diastolicDangerThreshold(thresholdDTO.getDiastolicDangerThreshold())
                    .diastolicEmergencyThreshold(thresholdDTO.getDiastolicEmergencyThreshold())
                    .build();

            configService.updateGlobalThreshold(HARDCODED_HOSPITAL_ID, finalEntity);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ngưỡng cảnh báo hệ thống toàn viện thành công!");
            return "redirect:/hospital/config";
        } catch (IllegalArgumentException e) {
            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("errorMessage", "Lỗi logic chéo: " + e.getMessage());
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", new com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO());
            return "hospital/config";
        } catch (Exception e) {
            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", new com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO());
            return "hospital/config";
        }
    }

    @PostMapping("/config/guides/add")
    public String addEmergencyGuide(@jakarta.validation.Valid @ModelAttribute("newGuide") com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO newGuideDTO,
                                    org.springframework.validation.BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("threshold", configService.getGlobalThreshold(HARDCODED_HOSPITAL_ID));
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", newGuideDTO);
            model.addAttribute("errorMessage", "Dữ liệu hướng dẫn không hợp lệ. Vui lòng kiểm tra lại viền đỏ hoặc độ dài ký tự.");
            return "hospital/config";
        }

        try {
            configService.addEmergencyGuide(
                    HARDCODED_HOSPITAL_ID,
                    newGuideDTO.getAlertLevel(),
                    newGuideDTO.getMetricType(),
                    newGuideDTO.getTitle(),
                    newGuideDTO.getInstructionContent()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Thêm hướng dẫn xử lý khẩn cấp mới thành công!");
            return "redirect:/hospital/config";
        } catch (IllegalArgumentException e) {
            model.addAttribute("threshold", configService.getGlobalThreshold(HARDCODED_HOSPITAL_ID));
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", newGuideDTO);
            model.addAttribute("errorMessage", "Lỗi nghiệp vụ: " + e.getMessage());
            return "hospital/config";
        } catch (Exception e) {
            model.addAttribute("threshold", configService.getGlobalThreshold(HARDCODED_HOSPITAL_ID));
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", newGuideDTO);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            return "hospital/config";
        }
    }

    @PostMapping("/config/guides/edit/{id}")
    public String editEmergencyGuide(@PathVariable("id") Integer id,
                                     @RequestParam("instructionContent") String instructionContent,
                                     RedirectAttributes redirectAttributes) {
        try {
            configService.editEmergencyGuideContent(id, instructionContent);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nội dung hướng dẫn khẩn cấp thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu hướng dẫn: " + e.getMessage());
        }
        return "redirect:/hospital/config";
    }
}