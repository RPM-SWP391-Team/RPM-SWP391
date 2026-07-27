package com.rpm.remotepatientmonitoring.controller.hospital;

import com.rpm.remotepatientmonitoring.model.AppRating;
import com.rpm.remotepatientmonitoring.model.DoctorRating;
import com.rpm.remotepatientmonitoring.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hospital/ratings")
public class HospitalRatingController {

    @Autowired
    private RatingService ratingService;

    @GetMapping
    public String showRatings(
            @RequestParam(value = "doctorPage", defaultValue = "0") int doctorPage,
            @RequestParam(value = "appPage", defaultValue = "0") int appPage,
            @RequestParam(value = "doctorFilter", required = false) Integer doctorFilter,
            @RequestParam(value = "appFilter", required = false) Integer appFilter,
            Model model
    ) {
        Pageable docPageable = PageRequest.of(doctorPage, 10, Sort.by(Sort.Direction.DESC, "id"));
        Pageable appPageable = PageRequest.of(appPage, 10, Sort.by(Sort.Direction.DESC, "id"));

        Page<DoctorRating> doctorRatings = ratingService.getDoctorRatings(doctorFilter, docPageable);
        Page<AppRating> appRatings = ratingService.getAppRatings(appFilter, appPageable);

        model.addAttribute("doctorRatings", doctorRatings.getContent());
        model.addAttribute("doctorPageObj", doctorRatings);
        model.addAttribute("currentDoctorPage", doctorPage);
        model.addAttribute("doctorFilter", doctorFilter);

        model.addAttribute("appRatings", appRatings.getContent());
        model.addAttribute("appPageObj", appRatings);
        model.addAttribute("currentAppPage", appPage);
        model.addAttribute("appFilter", appFilter);

        return "hospital/ratings";
    }

    @PostMapping("/doctor/toggle-hide/{id}")
    public String toggleHideDoctorRating(
            @PathVariable("id") Integer id,
            @RequestParam(value = "doctorPage", defaultValue = "0") int doctorPage,
            @RequestParam(value = "appPage", defaultValue = "0") int appPage,
            @RequestParam(value = "doctorFilter", required = false) Integer doctorFilter,
            @RequestParam(value = "appFilter", required = false) Integer appFilter,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ratingService.toggleHideDoctorRating(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái ẩn/hiện đánh giá bác sĩ.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        
        StringBuilder redirectUrl = new StringBuilder("redirect:/hospital/ratings");
        appendParams(redirectUrl, doctorPage, appPage, doctorFilter, appFilter);
        return redirectUrl.toString();
    }

    @PostMapping("/app/toggle-hide/{id}")
    public String toggleHideAppRating(
            @PathVariable("id") Integer id,
            @RequestParam(value = "doctorPage", defaultValue = "0") int doctorPage,
            @RequestParam(value = "appPage", defaultValue = "0") int appPage,
            @RequestParam(value = "doctorFilter", required = false) Integer doctorFilter,
            @RequestParam(value = "appFilter", required = false) Integer appFilter,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ratingService.toggleHideAppRating(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái ẩn/hiện đánh giá ứng dụng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        StringBuilder redirectUrl = new StringBuilder("redirect:/hospital/ratings");
        appendParams(redirectUrl, doctorPage, appPage, doctorFilter, appFilter);
        return redirectUrl.toString();
    }

    private void appendParams(StringBuilder sb, int docPage, int appPage, Integer docFilter, Integer appFilter) {
        sb.append("?doctorPage=").append(docPage)
          .append("&appPage=").append(appPage);
        if (docFilter != null) {
            sb.append("&doctorFilter=").append(docFilter);
        }
        if (appFilter != null) {
            sb.append("&appFilter=").append(appFilter);
        }
    }
}
