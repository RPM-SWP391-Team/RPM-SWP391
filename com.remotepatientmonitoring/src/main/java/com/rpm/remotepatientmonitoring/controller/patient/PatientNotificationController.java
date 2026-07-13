package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/patient/notifications")
public class PatientNotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PatientRepository patientRepository;

    // API 1: Đánh dấu một thông báo là đã đọc và chuyển hướng người dùng
    @GetMapping("/{id}/read-redirect")
    public String markAsReadAndRedirect(@PathVariable Integer id, HttpServletRequest request) {
        // Tìm thông báo bằng ID
        Optional<Notification> notificationOptional = notificationRepository.findById(id);

        String targetUrl = null;

        // Kiểm tra xem thông báo có tồn tại trong cơ sở dữ liệu hay không
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            // Nếu thông báo chưa được đọc (isRead là false)
            if (notification.getIsRead() == false) {
                notification.setIsRead(true); // Đổi thành đã đọc
                notificationRepository.save(notification); // Lưu lại vào cơ sở dữ liệu
            }

            // Chuyển hướng người dùng đến trang chức năng tương ứng với loại thông báo
            String type = notification.getNotificationType();
            if (type != null) {
                if (type.startsWith("EXERCISE") || type.equals("EXERCISE_REMINDER") || type.equals("EXERCISE_STREAK_MILESTONE") || type.equals("EXERCISE_BP_REMINDER") || type.equals("EXERCISE_INACTIVITY_REMINDER")) {
                    targetUrl = "/patient/exercise";
                } else if (type.startsWith("DIET") || type.equals("DIET_REMINDER")) {
                    targetUrl = "/patient/nutrition";
                } else if (type.contains("MED_REMINDER")) {
                    targetUrl = "/patient/adherence";
                } else if (type.equals("HEALTH_LOG_REMINDER")) {
                    targetUrl = "/patient/dashboard";
                }
            }
        }

        if (targetUrl != null) {
            return "redirect:" + targetUrl;
        }

        // Chuyển hướng người dùng quay lại trang cũ hoặc trang chủ dashboard
        String referer = request.getHeader("Referer");
        if (referer != null) {
            return "redirect:" + referer;
        } else {
            return "redirect:/patient/dashboard";
        }
    }

    // Hàm phụ trợ: Lấy thông tin bệnh nhân đang đăng nhập hiện tại
    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        if (auth.isAuthenticated() == false) {
            return null;
        }
        if (auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof Account) {
            Account account = (Account) principal;
            Optional<Patient> patientOpt = patientRepository.findByAccountId(account.getId());
            if (patientOpt.isPresent()) {
                return patientOpt.get();
            }
        } else if (principal instanceof User) {
            User user = (User) principal;
            String email = user.getUsername();
            Optional<Account> accountOpt = accountRepository.findByEmail(email);
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                Optional<Patient> patientOpt = patientRepository.findByAccountId(account.getId());
                if (patientOpt.isPresent()) {
                    return patientOpt.get();
                }
            }
        }
        return null;
    }

    // API 2: Đánh dấu TẤT CẢ thông báo là đã đọc và chuyển hướng người dùng
    @GetMapping("/read-all-redirect")
    public String markAllAsReadAndRedirect(HttpServletRequest request) {
        // Lấy bệnh nhân hiện tại
        Patient patient = getCurrentPatient();
        if (patient != null) {
            // Lấy danh sách tất cả thông báo của bệnh nhân này
            List<Notification> allNotifications = notificationRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());

            // Lọc ra các thông báo chưa đọc bằng vòng lặp for cơ bản
            List<Notification> unreadNotifications = new ArrayList<Notification>();
            for (int i = 0; i < allNotifications.size(); i++) {
                Notification notification = allNotifications.get(i);
                if (notification.getIsRead() == false) {
                    unreadNotifications.add(notification);
                }
            }

            // Duyệt danh sách các thông báo chưa đọc và đổi thành đã đọc
            for (int i = 0; i < unreadNotifications.size(); i++) {
                Notification notification = unreadNotifications.get(i);
                notification.setIsRead(true);
            }

            // Lưu tất cả các thông báo đã sửa đổi vào cơ sở dữ liệu
            notificationRepository.saveAll(unreadNotifications);
        }

        // Chuyển hướng người dùng quay lại trang cũ hoặc trang chủ dashboard
        String referer = request.getHeader("Referer");
        if (referer != null) {
            return "redirect:" + referer;
        } else {
            return "redirect:/patient/dashboard";
        }
    }
}
