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

        com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO thresholdDTO =
                com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO.builder()
                        .id(thresholdEntity.getId())
                        .glucoseHypoThreshold(thresholdEntity.getGlucoseHypoThreshold())
                        .glucoseNormalMax(thresholdEntity.getGlucoseNormalMax())
                        .glucoseHighMax(thresholdEntity.getGlucoseHighMax())
                        .systolicNormalMax(thresholdEntity.getSystolicNormalMax())
                        .systolicWarningMin(thresholdEntity.getSystolicWarningMin())
                        .systolicWarningMax(thresholdEntity.getSystolicWarningMax())
                        .systolicDangerMin(thresholdEntity.getSystolicDangerMin())
                        .systolicDangerMax(thresholdEntity.getSystolicDangerMax())
                        .systolicEmergencyThreshold(thresholdEntity.getSystolicEmergencyThreshold())
                        .diastolicNormalMax(thresholdEntity.getDiastolicNormalMax())
                        .diastolicWarningMin(thresholdEntity.getDiastolicWarningMin())
                        .diastolicWarningMax(thresholdEntity.getDiastolicWarningMax())
                        .diastolicDangerMin(thresholdEntity.getDiastolicDangerMin())
                        .diastolicDangerMax(thresholdEntity.getDiastolicDangerMax())
                        .diastolicEmergencyThreshold(thresholdEntity.getDiastolicEmergencyThreshold())
                        .build();

        model.addAttribute("threshold", thresholdDTO);
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

        if (bindingResult.hasErrors()) {
            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", new com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO());
            model.addAttribute("errorMessage", "Lỗi định dạng: Ô nhập liệu chứa ký tự chữ hoặc bị bỏ trống. Vui lòng kiểm tra lại.");
            return "hospital/config";
        }

        try {
            AlertThreshold finalEntity = AlertThreshold.builder()
                    .glucoseHypoThreshold(thresholdDTO.getGlucoseHypoThreshold())
                    .glucoseNormalMax(thresholdDTO.getGlucoseNormalMax())
                    .glucoseHighMax(thresholdDTO.getGlucoseHighMax())
                    .systolicNormalMax(thresholdDTO.getSystolicNormalMax())
                    .systolicWarningMin(thresholdDTO.getSystolicWarningMin())
                    .systolicWarningMax(thresholdDTO.getSystolicWarningMax())
                    .systolicDangerMin(thresholdDTO.getSystolicDangerMin())
                    .systolicDangerMax(thresholdDTO.getSystolicDangerMax())
                    .systolicEmergencyThreshold(thresholdDTO.getSystolicEmergencyThreshold())
                    .diastolicNormalMax(thresholdDTO.getDiastolicNormalMax())
                    .diastolicWarningMin(thresholdDTO.getDiastolicWarningMin())
                    .diastolicWarningMax(thresholdDTO.getDiastolicWarningMax())
                    .diastolicDangerMin(thresholdDTO.getDiastolicDangerMin())
                    .diastolicDangerMax(thresholdDTO.getDiastolicDangerMax())
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
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", new com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO());
            return "hospital/config";
        }
    }

    @PostMapping("/config/guide/add")
    public String addEmergencyGuide(@jakarta.validation.Valid @ModelAttribute("newGuide") com.rpm.remotepatientmonitoring.dto.EmergencyGuideDTO guideDTO,
                                    org.springframework.validation.BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            AlertThreshold thresholdEntity = configService.getGlobalThreshold(HARDCODED_HOSPITAL_ID);
            com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO thresholdDTO =
                    com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO.builder()
                            .id(thresholdEntity.getId())
                            .glucoseHypoThreshold(thresholdEntity.getGlucoseHypoThreshold())
                            .glucoseNormalMax(thresholdEntity.getGlucoseNormalMax())
                            .glucoseHighMax(thresholdEntity.getGlucoseHighMax())
                            .systolicNormalMax(thresholdEntity.getSystolicNormalMax())
                            .systolicWarningMin(thresholdEntity.getSystolicWarningMin())
                            .systolicWarningMax(thresholdEntity.getSystolicWarningMax())
                            .systolicDangerMin(thresholdEntity.getSystolicDangerMin())
                            .systolicDangerMax(thresholdEntity.getSystolicDangerMax())
                            .systolicEmergencyThreshold(thresholdEntity.getSystolicEmergencyThreshold())
                            .diastolicNormalMax(thresholdEntity.getDiastolicNormalMax())
                            .diastolicWarningMin(thresholdEntity.getDiastolicWarningMin())
                            .diastolicWarningMax(thresholdEntity.getDiastolicWarningMax())
                            .diastolicDangerMin(thresholdEntity.getDiastolicDangerMin())
                            .diastolicDangerMax(thresholdEntity.getDiastolicDangerMax())
                            .diastolicEmergencyThreshold(thresholdEntity.getDiastolicEmergencyThreshold())
                            .build();

            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", guideDTO);
            model.addAttribute("errorMessage", "Dữ liệu nhập hướng dẫn xử lý khẩn cấp không hợp lệ!");
            return "hospital/config";
        }

        try {
            configService.addEmergencyGuide(
                    HARDCODED_HOSPITAL_ID,
                    guideDTO.getAlertLevel(),
                    guideDTO.getMetricType(),
                    guideDTO.getTitle(),
                    guideDTO.getInstructionContent()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Thêm hướng dẫn xử lý khẩn cấp thành công!");
            return "redirect:/hospital/config";
        } catch (IllegalArgumentException e) {
            AlertThreshold thresholdEntity = configService.getGlobalThreshold(HARDCODED_HOSPITAL_ID);
            com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO thresholdDTO =
                    com.rpm.remotepatientmonitoring.dto.AlertThresholdsDTO.builder()
                            .id(thresholdEntity.getId())
                            .glucoseHypoThreshold(thresholdEntity.getGlucoseHypoThreshold())
                            .glucoseNormalMax(thresholdEntity.getGlucoseNormalMax())
                            .glucoseHighMax(thresholdEntity.getGlucoseHighMax())
                            .systolicNormalMax(thresholdEntity.getSystolicNormalMax())
                            .systolicWarningMin(thresholdEntity.getSystolicWarningMin())
                            .systolicWarningMax(thresholdEntity.getSystolicWarningMax())
                            .systolicDangerMin(thresholdEntity.getSystolicDangerMin())
                            .systolicDangerMax(thresholdEntity.getSystolicDangerMax())
                            .systolicEmergencyThreshold(thresholdEntity.getSystolicEmergencyThreshold())
                            .diastolicNormalMax(thresholdEntity.getDiastolicNormalMax())
                            .diastolicWarningMin(thresholdEntity.getDiastolicWarningMin())
                            .diastolicWarningMax(thresholdEntity.getDiastolicWarningMax())
                            .diastolicDangerMin(thresholdEntity.getDiastolicDangerMin())
                            .diastolicDangerMax(thresholdEntity.getDiastolicDangerMax())
                            .diastolicEmergencyThreshold(thresholdEntity.getDiastolicEmergencyThreshold())
                            .build();

            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("guides", configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID));
            model.addAttribute("newGuide", guideDTO);
            model.addAttribute("errorMessage", e.getMessage());
            return "hospital/config";
        }
    }

    @PostMapping("/config/guide/edit")
    public String editEmergencyGuide(@RequestParam("guideId") Integer guideId,
                                     @RequestParam("instructionContent") String instructionContent,
                                     RedirectAttributes redirectAttributes) {
        try {
            configService.editEmergencyGuideContent(guideId, instructionContent);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nội dung hướng dẫn khẩn cấp thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật: " + e.getMessage());
        }
        return "redirect:/hospital/config";
    }
}