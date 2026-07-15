package com.rpm.remotepatientmonitoring.controller.hopital;

import com.rpm.remotepatientmonitoring.model.AlertThreshold;
import com.rpm.remotepatientmonitoring.model.EmergencyGuide;
import com.rpm.remotepatientmonitoring.model.EmergencyProtocol;
import com.rpm.remotepatientmonitoring.service.hopital.HospitalConfigService;
import com.rpm.remotepatientmonitoring.dto.hopital.AlertThresholdsDTO;
import com.rpm.remotepatientmonitoring.dto.hopital.EmergencyGuideDTO;
import com.rpm.remotepatientmonitoring.dto.hopital.EmergencyProtocolDTO;
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

    // ==========================================
    // MÀN HÌNH 1: CẤU HÌNH NGƯỠNG LÂM SÀNG
    // ==========================================
    private void prepareModelForThresholdsPage(Model model) {
        AlertThreshold thresholdEntity = configService.getGlobalThreshold(HARDCODED_HOSPITAL_ID);
        AlertThresholdsDTO thresholdDTO = AlertThresholdsDTO.builder()
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
    }

    @GetMapping("/config/thresholds")
    public String getThresholdsPage(Model model) {
        prepareModelForThresholdsPage(model);
        return "hospital/config-thresholds";
    }

    @PostMapping("/config/thresholds/update")
    public String updateThresholds(@jakarta.validation.Valid @ModelAttribute("threshold") AlertThresholdsDTO thresholdDTO,
                                   org.springframework.validation.BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("errorMessage", "Lỗi định dạng: Ô nhập liệu chứa ký tự chữ hoặc bị bỏ trống. Vui lòng kiểm tra lại.");
            return "hospital/config-thresholds";
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
            return "redirect:/hospital/config/thresholds";
        } catch (IllegalArgumentException e) {
            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("errorMessage", "Lỗi logic chéo: " + e.getMessage());
            return "hospital/config-thresholds";
        } catch (Exception e) {
            model.addAttribute("threshold", thresholdDTO);
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "hospital/config-thresholds";
        }
    }

    // ==========================================
    // MÀN HÌNH 2: CẨM NANG & CHỈ DẪN XỬ LÝ
    // ==========================================
    private void prepareModelForProtocolsPage(Model model) {
        List<EmergencyGuide> guides = configService.getEmergencyGuides(HARDCODED_HOSPITAL_ID);
        List<EmergencyProtocol> protocols = configService.getEmergencyProtocols(HARDCODED_HOSPITAL_ID);
        model.addAttribute("guides", guides);
        model.addAttribute("protocols", protocols);
        if (!model.containsAttribute("newGuide")) {
            model.addAttribute("newGuide", new EmergencyGuideDTO());
        }
        if (!model.containsAttribute("newProtocol")) {
            model.addAttribute("newProtocol", new EmergencyProtocolDTO());
        }
    }

    @GetMapping("/config/protocols")
    public String getProtocolsPage(Model model) {
        prepareModelForProtocolsPage(model);
        return "hospital/config-protocols";
    }

    @PostMapping("/config/guide/add")
    public String addEmergencyGuide(@jakarta.validation.Valid @ModelAttribute("newGuide") EmergencyGuideDTO guideDTO,
                                    org.springframework.validation.BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareModelForProtocolsPage(model);
            model.addAttribute("newGuide", guideDTO);
            model.addAttribute("errorMessage", "Dữ liệu nhập hướng dẫn xử lý khẩn cấp không hợp lệ!");
            return "hospital/config-protocols";
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
            return "redirect:/hospital/config/protocols";
        } catch (IllegalArgumentException e) {
            prepareModelForProtocolsPage(model);
            model.addAttribute("newGuide", guideDTO);
            model.addAttribute("errorMessage", e.getMessage());
            return "hospital/config-protocols";
        }
    }

    @PostMapping("/config/guide/edit")
    public String editEmergencyGuide(@RequestParam("guideId") Integer guideId,
                                     @RequestParam("instructionContent") String instructionContent,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            configService.editEmergencyGuideContent(guideId, instructionContent);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nội dung hướng dẫn khẩn cấp thành công!");
            return "redirect:/hospital/config/protocols"; // Chỉ redirect khi thành công
        } catch (Exception e) {
            // Lỗi thì trả thẳng về View kèm thông báo lỗi
            prepareModelForProtocolsPage(model);
            model.addAttribute("errorMessage", "Không thể cập nhật: " + e.getMessage());
            return "hospital/config-protocols";
        }
    }

    @PostMapping("/config/protocol/add")
    public String addEmergencyProtocol(@jakarta.validation.Valid @ModelAttribute("newProtocol") EmergencyProtocolDTO protocolDTO,
                                       org.springframework.validation.BindingResult bindingResult,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareModelForProtocolsPage(model);
            model.addAttribute("newProtocol", protocolDTO);
            model.addAttribute("errorMessage", "Dữ liệu nhập cẩm nang nhận biết không hợp lệ!");
            return "hospital/config-protocols";
        }
        try {
            configService.addEmergencyProtocol(
                    HARDCODED_HOSPITAL_ID,
                    protocolDTO.getConditionType(),
                    protocolDTO.getTitle(),
                    protocolDTO.getWarningSigns(),
                    protocolDTO.getInstructionContent()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Thêm cẩm nang nhận biết thành công!");
            return "redirect:/hospital/config/protocols";
        } catch (IllegalArgumentException e) {
            prepareModelForProtocolsPage(model);
            model.addAttribute("newProtocol", protocolDTO);
            model.addAttribute("errorMessage", e.getMessage());
            return "hospital/config-protocols";
        }
    }

    @PostMapping("/config/protocol/edit")
    public String editEmergencyProtocol(@RequestParam("protocolId") Integer protocolId,
                                        @RequestParam("warningSigns") String warningSigns,
                                        @RequestParam("instructionContent") String instructionContent,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            configService.editEmergencyProtocolContent(protocolId, warningSigns, instructionContent);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật cẩm nang nhận biết thành công!");
            return "redirect:/hospital/config/protocols"; // Chỉ redirect khi thành công
        } catch (Exception e) {
            // Lỗi thì trả thẳng về View kèm thông báo lỗi
            prepareModelForProtocolsPage(model);
            model.addAttribute("errorMessage", "Không thể cập nhật cẩm nang: " + e.getMessage());
            return "hospital/config-protocols";
        }
    }

    @DeleteMapping("/config/guide/delete/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> deleteEmergencyGuide(@PathVariable("id") Integer id) {
        try {
            configService.deleteEmergencyGuide(id);
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    @DeleteMapping("/config/protocol/delete/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> deleteEmergencyProtocol(@PathVariable("id") Integer id) {
        try {
            configService.deleteEmergencyProtocol(id);
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }
}