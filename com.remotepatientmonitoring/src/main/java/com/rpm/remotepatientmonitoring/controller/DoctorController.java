package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.rpm.remotepatientmonitoring.dto.DoctorDTO;

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
            model.addAttribute("newDoctor", new DoctorDTO());
        }
        return "admin/doctors";
    }

    @PostMapping("/add")
    public String addDoctor(@jakarta.validation.Valid @ModelAttribute("newDoctor") DoctorDTO doctorDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        // Bắt lỗi Validation thô (Trống trường, sai độ dài mật khẩu, sai định dạng Regex)
        if (bindingResult.hasErrors()) {
            List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
            model.addAttribute("doctors", doctors);
            model.addAttribute("newDoctor", doctorDto);
            model.addAttribute("errorMessage", "Lỗi nhập liệu: Vui lòng kiểm tra lại các trường báo đỏ.");
            return "admin/doctors";
        }

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
            return "redirect:/admin/doctors";
        } catch (IllegalArgumentException e) {
            // Bắt lỗi trùng lặp từ Database và map chính xác vào ô nhập liệu để báo đỏ cục bộ
            if (e.getMessage().contains("Mã bác sĩ")) {
                bindingResult.rejectValue("doctorCode", "error.doctorCode", e.getMessage());
            } else if (e.getMessage().contains("Số điện thoại")) {
                bindingResult.rejectValue("phone", "error.phone", e.getMessage());
            } else if (e.getMessage().contains("Email")) {
                bindingResult.rejectValue("email", "error.email", e.getMessage());
            } else {
                model.addAttribute("errorMessage", e.getMessage());
            }

            List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
            model.addAttribute("doctors", doctors);
            model.addAttribute("newDoctor", doctorDto);
            return "admin/doctors";
        } catch (Exception e) {
            List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
            model.addAttribute("doctors", doctors);
            model.addAttribute("newDoctor", doctorDto);
            model.addAttribute("errorMessage", "Hệ thống gặp sự cố: " + e.getMessage());
            return "admin/doctors";
        }
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateDoctor(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deactivateDoctor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã vô hiệu hóa tài khoản và điều chuyển bệnh nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thực thi: " + e.getMessage());
        }
        return "redirect:/admin/doctors";
    }


}