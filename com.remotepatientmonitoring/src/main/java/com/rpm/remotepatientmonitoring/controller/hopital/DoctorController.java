package com.rpm.remotepatientmonitoring.controller.hopital;

import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.service.hopital.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.rpm.remotepatientmonitoring.dto.hopital.DoctorDTO;
import com.rpm.remotepatientmonitoring.dto.hopital.DoctorEditDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/hospital/doctors")
public class DoctorController {

    private final Integer HARDCODED_HOSPITAL_ID = 1;

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public String listDoctors(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "specialty", required = false) String specialty,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              Model model) {

        int pageSize = 10; // Số bác sĩ trên 1 trang
        // Tạo phân trang và KÈM THEO sắp xếp ID giảm dần (mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<Doctor> doctorPage = doctorService.searchAndFilterAllDoctors(search, specialty, pageable);

        model.addAttribute("doctors", doctorPage.getContent());
        model.addAttribute("doctorPage", doctorPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", doctorPage.getTotalPages());
        model.addAttribute("totalItems", doctorPage.getTotalElements());

        model.addAttribute("searchKeyword", search != null ? search : "");
        model.addAttribute("selectedSpecialty", specialty != null ? specialty : "");

        if (!model.containsAttribute("doctorDto")) {
            DoctorDTO newDto = new DoctorDTO();
            newDto.setDoctorCode(doctorService.generateNextDoctorCode());
            model.addAttribute("doctorDto", newDto);
        }
        if (!model.containsAttribute("doctorEditDto")) {
            model.addAttribute("doctorEditDto", new DoctorEditDTO());
        }

        return "hospital/doctors";
    }

    @PostMapping("/add")
    public String addDoctor(@Valid @ModelAttribute("doctorDto") DoctorDTO doctorDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        // BỔ SUNG ĐOẠN NÀY: Nếu lỗi rơi vào hàm check AsserTrue thì gán nó vào ô fullName luôn
        if (bindingResult.hasFieldErrors("fullNameValid")) {
            bindingResult.rejectValue("fullName", "error.doctorDto",
                    bindingResult.getFieldError("fullNameValid").getDefaultMessage());
        }

        // Chặn đứng chữ rác: Nếu DTO dính lỗi Regex, dừng luồng dữ liệu lập tức và trả về giao diện kèm thông báo
        if (bindingResult.hasErrors()) {
            // SỬA CHUẨN: Đồng bộ phân trang khi trả về lỗi Validation tránh crash giao diện
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
            Page<Doctor> doctorPage = doctorService.searchAndFilterAllDoctors(null, null, pageable);
            model.addAttribute("doctors", doctorPage.getContent());
            model.addAttribute("doctorPage", doctorPage);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", doctorPage.getTotalPages());
            model.addAttribute("totalItems", doctorPage.getTotalElements());

            model.addAttribute("searchKeyword", "");
            model.addAttribute("selectedSpecialty", "");
            model.addAttribute("doctorDto", doctorDto);
            model.addAttribute("doctorEditDto", new DoctorEditDTO());
            model.addAttribute("errorMessage", "Đăng ký thất bại: Biểu mẫu chứa thông tin rác hoặc sai định dạng chữ cái Tiếng Việt!");
            return "hospital/doctors";
        }

        try {
            doctorService.createDoctor(
                    HARDCODED_HOSPITAL_ID,
                    doctorDto.getDoctorCode(),
                    doctorDto.getFullName(),
                    doctorDto.getPhone(),
                    doctorDto.getEmail(),
                    doctorDto.getGender(),
                    null,
                    doctorDto.getSpecialty(),
                    doctorDto.getCapacityLimit()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Thêm bác sĩ mới và gửi mail kích hoạt thành công!");
            return "redirect:/hospital/doctors";
        } catch (IllegalArgumentException e) {
            // Đẩy lỗi trùng lặp/nghiệp vụ từ DB về đúng ô input trên form
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
            model.addAttribute("doctorEditDto", new DoctorEditDTO());
            return "hospital/doctors";
        } catch (Exception e) {
            List<Doctor> doctors = doctorService.getDoctorsByHospital(HARDCODED_HOSPITAL_ID);
            model.addAttribute("doctors", doctors);
            model.addAttribute("doctorDto", doctorDto);
            model.addAttribute("doctorEditDto", new DoctorEditDTO());
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

    @GetMapping("/detail/{id}")
    @ResponseBody
    public ResponseEntity<?> getDoctorDetail(@PathVariable("id") int id) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            List<Patient> patients = doctorService.getPatientsByDoctorId(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", doctor.getId());
            response.put("doctorCode", doctor.getDoctorCode());
            response.put("fullName", doctor.getFullName());
            response.put("phone", doctor.getPhone());
            response.put("specialty", doctor.getSpecialty());
            response.put("gender", doctor.getGender());
            response.put("capacityLimit", doctor.getCapacityLimit());
            response.put("currentPatientCount", doctor.getCurrentPatientCount());
            response.put("isActive", doctor.getIsActive());
            response.put("createdAt", doctor.getCreatedAt().toString());
            response.put("email", doctor.getAccount() != null ? doctor.getAccount().getEmail() : "");
            
            List<Map<String, Object>> patientList = new ArrayList<>();
            for (Patient p : patients) {
                Map<String, Object> pMap = new HashMap<>();
                pMap.put("id", p.getId());
                pMap.put("patientCode", p.getPatientCode() != null ? p.getPatientCode() : "");
                pMap.put("fullName", p.getFullName());
                pMap.put("phone", p.getPhone());
                pMap.put("gender", p.getGender());
                pMap.put("status", p.getStatus());
                patientList.add(pMap);
            }
            response.put("patients", patientList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/edit/{id}")
    public String editDoctor(@PathVariable("id") Integer id,
                             @Valid @ModelAttribute("doctorEditDto") DoctorEditDTO doctorEditDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasFieldErrors("fullNameValid")) {
            bindingResult.rejectValue("fullName", "error.doctorEditDto",
                    bindingResult.getFieldError("fullNameValid").getDefaultMessage());
        }

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder("Cập nhật thất bại: ");
            bindingResult.getFieldErrors().forEach(error -> sb.append(error.getDefaultMessage()).append(" "));
            redirectAttributes.addFlashAttribute("errorMessage", sb.toString());
            redirectAttributes.addFlashAttribute("doctorEditDto", doctorEditDto);
            return "redirect:/hospital/doctors";
        }

        try {
            doctorService.updateDoctor(id, doctorEditDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin bác sĩ thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
            redirectAttributes.addFlashAttribute("doctorEditDto", doctorEditDto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hệ thống gặp sự cố: " + e.getMessage());
            redirectAttributes.addFlashAttribute("doctorEditDto", doctorEditDto);
        }
        return "redirect:/hospital/doctors";
    }
}