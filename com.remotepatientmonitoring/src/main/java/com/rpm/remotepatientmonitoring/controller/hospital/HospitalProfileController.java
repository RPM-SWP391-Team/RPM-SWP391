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

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.HospitalRepository hospitalRepository;

    private Integer getLoggedHospitalId() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.rpm.remotepatientmonitoring.config.CustomUserDetails) {
            com.rpm.remotepatientmonitoring.config.CustomUserDetails userDetails = (com.rpm.remotepatientmonitoring.config.CustomUserDetails) auth.getPrincipal();
            return hospitalRepository.findByAccountId(userDetails.getAccount().getId())
                    .map(com.rpm.remotepatientmonitoring.model.Hospital::getId)
                    .orElse(HARDCODED_HOSPITAL_ID);
        }
        return HARDCODED_HOSPITAL_ID;
    }

    @GetMapping("/profile")
    public String getProfilePage(Model model) {
        if (!model.containsAttribute("profileDTO")) {
            model.addAttribute("profileDTO", profileService.getProfileByHospitalId(getLoggedHospitalId()));
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
            profileService.updateProfile(getLoggedHospitalId(), profileDTO);
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