package com.rpm.remotepatientmonitoring.controller.hospital;

import com.rpm.remotepatientmonitoring.dto.hospital.HospitalProfileDTO;
import com.rpm.remotepatientmonitoring.service.hospital.HospitalProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hospital")
public class HospitalProfileController {

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @Autowired
    private HospitalProfileService profileService;

    @GetMapping("/profile")
    public String getProfilePage(Model model) {
        if (!model.containsAttribute("profileDTO")) {
            model.addAttribute("profileDTO", profileService.getProfileByHospitalId(HARDCODED_HOSPITAL_ID));
        }
        return "hospital/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@jakarta.validation.Valid @ModelAttribute("profileDTO") HospitalProfileDTO profileDTO,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Dữ liệu nhập không hợp lệ. Vui lòng kiểm tra lại các trường báo đỏ.");
            return "hospital/profile";
        }

        try {
            profileService.updateProfile(HARDCODED_HOSPITAL_ID, profileDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin bệnh viện thành công!");
            return "redirect:/hospital/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "hospital/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "hospital/profile";
        }
    }
}