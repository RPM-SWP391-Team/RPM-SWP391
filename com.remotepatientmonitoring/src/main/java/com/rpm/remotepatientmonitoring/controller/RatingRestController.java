package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingRestController {

    @Autowired
    private RatingService ratingService;

    @PostMapping("/doctor")
    public ResponseEntity<Map<String, Object>> submitDoctorRating(
            @RequestBody DoctorRatingRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thực hiện đánh giá.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Account account = userDetails.getAccount();
            ratingService.submitDoctorRating(request.getAppointmentId(), request.getRatingValue(), request.getComment(), account);
            response.put("success", true);
            response.put("message", "Đánh giá bác sĩ thành công!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi máy chủ: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/app")
    public ResponseEntity<Map<String, Object>> submitAppRating(
            @RequestBody AppRatingRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thực hiện đánh giá.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Account account = userDetails.getAccount();
            ratingService.submitAppRating(request.getRatingValue(), request.getComment(), account);
            response.put("success", true);
            response.put("message", "Cảm ơn bạn đã gửi đánh giá ứng dụng!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi máy chủ: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    public static class DoctorRatingRequest {
        private Integer appointmentId;
        private Integer ratingValue;
        private String comment;

        public Integer getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
        public Integer getRatingValue() { return ratingValue; }
        public void setRatingValue(Integer ratingValue) { this.ratingValue = ratingValue; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    public static class AppRatingRequest {
        private Integer ratingValue;
        private String comment;

        public Integer getRatingValue() { return ratingValue; }
        public void setRatingValue(Integer ratingValue) { this.ratingValue = ratingValue; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}
