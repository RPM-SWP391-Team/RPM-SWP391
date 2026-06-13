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
@RequestMapping("/admin/config")
public class HospitalConfigController {

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @Autowired
    private HospitalConfigService configService;

    @GetMapping
    public String getConfigPage(Model model) {
        AlertThreshold threshold = configService.getGlobalThreshold(HARDCODED_HOSPITAL_ID);
        List<EmergencyGuide> guides = configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID);

        model.addAttribute("threshold", threshold);
        model.addAttribute("guides", guides);
        if (!model.containsAttribute("newGuide")) {
            model.addAttribute("newGuide", new EmergencyGuide());
        }
        return "admin/config";
    }

    @PostMapping("/thresholds/update")
    public String updateThresholds(@ModelAttribute("threshold") AlertThreshold threshold, 
                                   RedirectAttributes redirectAttributes) {
        try {
            configService.updateGlobalThreshold(HARDCODED_HOSPITAL_ID, threshold);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ngưỡng cảnh báo hệ thống thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dữ liệu: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    @PostMapping("/guides/add")
    public String addEmergencyGuide(@ModelAttribute("newGuide") EmergencyGuide newGuide, 
                                    RedirectAttributes redirectAttributes) {
        try {
            configService.addEmergencyGuide(
                    HARDCODED_HOSPITAL_ID,
                    newGuide.getAlertLevel(),
                    newGuide.getMetricType(),
                    newGuide.getTitle(),
                    newGuide.getInstructionContent()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Thêm hướng dẫn xử lý khẩn cấp mới thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dữ liệu: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    @PostMapping("/guides/edit/{id}")
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
        return "redirect:/admin/config";
    }
}
