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
import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/hospital/doctors")
public class DoctorController {

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public String listDoctors(Model model) {
        List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
        model.addAttribute("doctors", doctors);

        // ĐÃ SỬA: Tự sinh mã gợi ý tăng tiến gán vào form mới nếu không có lỗi từ redirect về
        if (!model.containsAttribute("doctorDto")) {
            DoctorDTO newDto = new DoctorDTO();
            newDto.setDoctorCode(doctorService.generateNextDoctorCode()); // Gọi hàm tự sinh mã
            model.addAttribute("doctorDto", newDto);
        }
        return "hospital/doctors";
    }

    @PostMapping("/add")
    public String addDoctor(@Valid @ModelAttribute("doctorDto") DoctorDTO doctorDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        // 🌟 1. CHẶN ĐỨNG CHỮ RÁC: Nếu DTO dính lỗi Regex/Trống, đóng băng luồng và quay xe ngay lập tức
        if (bindingResult.hasErrors()) {
            List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
            model.addAttribute("doctors", doctors);
            model.addAttribute("doctorDto", doctorDto);
            // Ép ngược thông báo lỗi tổng để khối th:if="${errorMessage}" ngoài trang danh sách hiển thị
            model.addAttribute("errorMessage", "Lỗi nhập liệu: Dữ liệu chứa ký tự không hợp lệ hoặc để trống!");
            return "hospital/doctors"; // Trả thẳng về trang để giữ nguyên cây dữ liệu lỗi của BindingResult
        }

        try {
            doctorService.createDoctor(
                    HARDCODED_HOSPITAL_ID,
                    doctorDto.getDoctorCode(),
                    doctorDto.getFullName(),
                    doctorDto.getPhone(),
                    doctorDto.getEmail(),
                    null,
                    doctorDto.getSpecialty(),
                    doctorDto.getCapacityLimit()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Thêm bác sĩ mới và gửi mail kích hoạt thành công!");
            return "redirect:/hospital/doctors";
        } catch (IllegalArgumentException e) {
            // 🌟 2. BẮT LỖI NGHIỆP VỤ (Email không tồn tại / Trùng lặp mã, số điện thoại)
            // Sửa triệt để mã lỗi thành "error.doctorDto" để Thymeleaf ánh xạ trúng thẻ invalid-feedback
            if (e.getMessage().contains("Mã bác sĩ")) {
                bindingResult.rejectValue("doctorCode", "error.doctorDto", e.getMessage());
            } else if (e.getMessage().contains("Họ và tên")) {
                bindingResult.rejectValue("fullName", "error.doctorDto", e.getMessage());
            } else if (e.getMessage().contains("Số điện thoại")) {
                bindingResult.rejectValue("phone", "error.doctorDto", e.getMessage());
            } else if (e.getMessage().contains("Email")) {
                bindingResult.rejectValue("email", "error.doctorDto", e.getMessage());
            } else {
                model.addAttribute("errorMessage", e.getMessage());
            }

            List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
            model.addAttribute("doctors", doctors);
            model.addAttribute("doctorDto", doctorDto);
            return "hospital/doctors";
        } catch (Exception e) {
            List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
            model.addAttribute("doctors", doctors);
            model.addAttribute("doctorDto", doctorDto);
            model.addAttribute("errorMessage", "Hệ thống gặp sự cố mạng: " + e.getMessage());
            return "hospital/doctors";
        }
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateDoctor(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deactivateDoctor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã vô hiệu hóa tài khoản và điều chuyển bệnh nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thực thi vô hiệu hóa: " + e.getMessage());
        }
        return "redirect:/hospital/doctors";
    }

    @PostMapping("/activate/{id}")
    public String activateDoctor(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.activateDoctor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt lại tài khoản bác sĩ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thực thi kích hoạt: " + e.getMessage());
        }
        return "redirect:/hospital/doctors";
    }
}