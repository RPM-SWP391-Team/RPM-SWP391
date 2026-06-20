package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hospital")
public class HospitalController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "hospital/dashboard";
    }

    @GetMapping("/doctor/create")
    public String createDoctorPage() {
        return "hospital/create-doctor";
    }

    @PostMapping("/doctor/create")
    public String createDoctor(
            @RequestParam Integer hospitalId,
            @RequestParam String doctorCode,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam(required = false) String password, // Có thể để trống để kích hoạt tự sinh mật khẩu
            @RequestParam String specialty,
            @RequestParam(defaultValue = "50") Integer capacityLimit, // Giá trị mặc định nếu form chưa có ô nhập
            Model model
    ) {
        try {
            // Gọi đúng thứ tự và đủ 8 tham số của Service
            doctorService.createDoctor(hospitalId, doctorCode, fullName, phone, email, password, specialty, capacityLimit);
            model.addAttribute("successMessage", "Tạo tài khoản bác sĩ thành công! Mật khẩu đã được gửi qua email.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "hospital/create-doctor";
    }


}