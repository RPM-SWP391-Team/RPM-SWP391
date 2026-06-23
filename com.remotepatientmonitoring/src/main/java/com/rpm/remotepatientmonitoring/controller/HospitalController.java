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
        return "redirect:/hospital/doctors";
    }
}