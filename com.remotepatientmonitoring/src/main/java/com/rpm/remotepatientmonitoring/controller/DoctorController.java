package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/doctors")
public class DoctorController {

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public String listDoctors(Model model) {
        List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
        model.addAttribute("doctors", doctors);
        if (!model.containsAttribute("newDoctor")) {
            model.addAttribute("newDoctor", new DoctorDto());
        }
        return "admin/doctors";
    }

    @PostMapping("/add")
    public String addDoctor(@ModelAttribute("newDoctor") DoctorDto doctorDto, 
                            RedirectAttributes redirectAttributes) {
        try {
            doctorService.createDoctor(
                    HARDCODED_HOSPITAL_ID,
                    doctorDto.getDoctorCode(),
                    doctorDto.getFullName(),
                    doctorDto.getPhone(),
                    doctorDto.getEmail(),
                    doctorDto.getPassword(),
                    doctorDto.getSpecialty(),
                    doctorDto.getCapacityLimit()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Thêm bác sĩ mới thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("newDoctor", doctorDto); // Return input to form
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không xác định: " + e.getMessage());
            redirectAttributes.addFlashAttribute("newDoctor", doctorDto);
        }
        return "redirect:/admin/doctors";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateDoctor(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deactivateDoctor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã vô hiệu hóa tài khoản bác sĩ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi vô hiệu hóa bác sĩ: " + e.getMessage());
        }
        return "redirect:/admin/doctors";
    }

    // Helper DTO for form binding
    public static class DoctorDto {
        private String doctorCode;
        private String fullName;
        private String phone;
        private String email;
        private String password;
        private String specialty;
        private Integer capacityLimit = 50;

        // Getters and Setters
        public String getDoctorCode() { return doctorCode; }
        public void setDoctorCode(String doctorCode) { this.doctorCode = doctorCode; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getSpecialty() { return specialty; }
        public void setSpecialty(String specialty) { this.specialty = specialty; }
        public Integer getCapacityLimit() { return capacityLimit; }
        public void setCapacityLimit(Integer capacityLimit) { this.capacityLimit = capacityLimit; }
    }
}
