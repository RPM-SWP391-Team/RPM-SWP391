package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;

@Service
public class RatingService {

    @Autowired
    private DoctorRatingRepository doctorRatingRepository;

    @Autowired
    private AppRatingRepository appRatingRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    private List<String> bannedWords;

    @jakarta.annotation.PostConstruct
    public void init() {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(
                        getClass().getResourceAsStream("/banned_words.txt"),
                        java.nio.charset.StandardCharsets.UTF_8))) {
            bannedWords = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(String::toLowerCase)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            bannedWords = java.util.Arrays.asList("đm", "vcl", "dkm", "ngu", "chó");
        }
    }

    private void checkBannedWords(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return;
        }
        String lowerComment = comment.toLowerCase();
        for (String word : bannedWords) {
            if (lowerComment.contains(word)) {
                throw new IllegalArgumentException("Nội dung nhận xét chứa từ ngữ không phù hợp. Vui lòng chỉnh sửa lại.");
            }
        }
    }
    @Transactional
    public DoctorRating submitDoctorRating(Integer appointmentId, Integer ratingValue, String comment, Account currentAccount) {
        checkBannedWords(comment);
        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("Nhận xét không được vượt quá 1000 ký tự.");
        }
        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5 sao.");
        }

        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch khám với ID: " + appointmentId));

        if (!"COMPLETED".equals(appt.getStatus())) {
            throw new IllegalStateException("Chỉ có thể đánh giá lịch khám đã hoàn thành.");
        }

        // Validate ownership: currentAccount must own this patient profile
        Patient patient = appt.getPatient();
        if (patient == null || patient.getAccount() == null || !patient.getAccount().getId().equals(currentAccount.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền đánh giá lịch khám của người khác.");
        }

        // Check duplicate rating (Unique constraint)
        if (doctorRatingRepository.existsByAppointmentId(appointmentId)) {
            throw new IllegalStateException("Lịch khám này đã được đánh giá trước đó.");
        }

        DoctorRating rating = DoctorRating.builder()
                .appointment(appt)
                .patient(patient)
                .doctor(appt.getDoctor())
                .ratingValue(ratingValue)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        return doctorRatingRepository.save(rating);
    }

    @Transactional
    public AppRating submitAppRating(Integer ratingValue, String comment, Account currentAccount) {
        if ("HOSPITAL_ADMIN".equals(currentAccount.getRole())) {
            throw new IllegalArgumentException("Chức năng này không áp dụng cho tài khoản quản trị viên.");
        }
        checkBannedWords(comment);
        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("Nhận xét không được vượt quá 1000 ký tự.");
        }
        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5 sao.");
        }

        // Check 1 rating per day limit
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long count = appRatingRepository.countByAccountIdAndCreatedAtAfter(currentAccount.getId(), startOfToday);
        if (count >= 1) {
            throw new IllegalStateException("Mỗi tài khoản chỉ được đánh giá ứng dụng tối đa 1 lần mỗi ngày.");
        }

        AppRating rating = AppRating.builder()
                .account(currentAccount)
                .ratingValue(ratingValue)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        return appRatingRepository.save(rating);
    }

    public void populateDoctorRatings(Doctor doctor) {
        if (doctor == null) return;
        long count = doctorRatingRepository.countByDoctorId(doctor.getId());
        Double avg = doctorRatingRepository.getAverageRatingByDoctorId(doctor.getId());
        doctor.setRatingCount(count);
        doctor.setAverageRating(avg != null ? avg : 0.0);
    }

    public void populateDoctorRatings(List<Doctor> doctors) {
        if (doctors == null) return;
        for (Doctor d : doctors) {
            populateDoctorRatings(d);
        }
    }

    public Page<DoctorRating> getDoctorRatings(Integer ratingFilter, Pageable pageable) {
        if (ratingFilter != null) {
            if (ratingFilter == 12) {
                return doctorRatingRepository.findByRatingValueIn(Arrays.asList(1, 2), pageable);
            } else {
                return doctorRatingRepository.findByRatingValueIn(Arrays.asList(ratingFilter), pageable);
            }
        }
        return doctorRatingRepository.findAll(pageable);
    }

    public Page<AppRating> getAppRatings(Integer ratingFilter, Pageable pageable) {
        if (ratingFilter != null) {
            if (ratingFilter == 12) {
                return appRatingRepository.findByRatingValueIn(Arrays.asList(1, 2), pageable);
            } else {
                return appRatingRepository.findByRatingValueIn(Arrays.asList(ratingFilter), pageable);
            }
        }
        return appRatingRepository.findAll(pageable);
    }

    @Transactional
    public void toggleHideDoctorRating(Integer ratingId) {
        DoctorRating rating = doctorRatingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá với ID: " + ratingId));
        rating.setIsHidden(!rating.getIsHidden());
        doctorRatingRepository.save(rating);
    }

    @Transactional
    public void toggleHideAppRating(Integer ratingId) {
        AppRating rating = appRatingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá với ID: " + ratingId));
        rating.setIsHidden(!rating.getIsHidden());
        appRatingRepository.save(rating);
    }
}
