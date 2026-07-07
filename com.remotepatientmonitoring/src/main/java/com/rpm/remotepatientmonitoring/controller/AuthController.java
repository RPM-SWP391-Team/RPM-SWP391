package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.service.AuthService;
import com.rpm.remotepatientmonitoring.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "verified", required = false) String verified,
            @RequestParam(value = "disabled", required = false) String disabled,
            @RequestParam(value = "resetSuccess", required = false) String resetSuccess,
            Model model) {
        if (disabled != null) {
            model.addAttribute("errorMessage", "Vui lòng xác thực email trước khi đăng nhập.");
            model.addAttribute("username", username);
            model.addAttribute("emailNotVerified", true);
            model.addAttribute("unverifiedEmail", username);
        } else if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không đúng!");
            model.addAttribute("username", username);
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Đăng xuất thành công!");
        }
        if (verified != null) {
            model.addAttribute("successMessage", "Xác thực email thành công! Bạn có thể đăng nhập.");
        }
        if (resetSuccess != null) {
            model.addAttribute("successMessage", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String dateOfBirth,
            @RequestParam String gender,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String emergencyContactName,
            @RequestParam(required = false) String emergencyContactPhone,
            Model model) {
        // Validate họ tên
        if (fullName == null || fullName.trim().isEmpty()) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Họ và tên không được để trống!");
            return "auth/register";
        }
        // Regex kiểm tra tên tiếng Việt: Chỉ chứa chữ cái và khoảng trắng, không chứa số hay kí tự đặc biệt
        String namePattern = "^[\\p{L}\\s]{2,50}$";
        if (!fullName.trim().matches(namePattern)) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Họ và tên chỉ được chứa chữ cái, khoảng trắng và độ dài từ 2 đến 50 ký tự!");
            return "auth/register";
        }
        // Validate số điện thoại
        if (phone == null || phone.trim().isEmpty()) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Số điện thoại không được để trống!");
            return "auth/register";
        }
        if (!phone.matches("^(0|\\+84)[0-9]{9,10}$")) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Số điện thoại không đúng định dạng!");
            return "auth/register";
        }
        // Validate ngày sinh
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Ngày sinh không được để trống!");
            return "auth/register";
        }
        try {
            java.time.LocalDate dob = java.time.LocalDate.parse(dateOfBirth);
            java.time.LocalDate today = java.time.LocalDate.now();
            if (dob.isAfter(today)) {
                preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                        emergencyContactPhone);
                model.addAttribute("errorMessage", "Ngày sinh không được ở tương lai!");
                return "auth/register";
            }
            int age = java.time.Period.between(dob, today).getYears();
            if (age > 120) {
                preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                        emergencyContactPhone);
                model.addAttribute("errorMessage", "Tuổi không được vượt quá 120!");
                return "auth/register";
            }
        } catch (Exception e) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Ngày sinh không đúng định dạng!");
            return "auth/register";
        }
        // Validate giới tính
        if (gender == null || gender.trim().isEmpty()) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Vui lòng chọn giới tính!");
            return "auth/register";
        }
        // Validate email
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Email không đúng định dạng!");
            return "auth/register";
        }
        // Validate password
        if (password.length() < 8) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Mật khẩu phải có ít nhất 8 ký tự!");
            return "auth/register";
        }
        if (!password.equals(confirmPassword)) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "auth/register";
        }
        // Validate email chưa tồn tại
        if (authService.emailExists(email)) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Email này đã được đăng ký!");
            return "auth/register";
        }
        // Validate phone chưa tồn tại
        if (authService.phoneExists(phone)) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Số điện thoại này đã được đăng ký!");
            return "auth/register";
        }

        // Validate SĐT khẩn cấp không trùng SĐT bệnh nhân
        if (emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty()
                && emergencyContactPhone.trim().equals(phone.trim())) {
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage",
                    "Số điện thoại người liên hệ khẩn cấp không được trùng với số điện thoại của bạn!");
            return "auth/register";
        }

        // ===== BƯỚC 1: Lưu DB (Account + Patient + OTP record) =====
        // Transaction commit khi registerPatient() return thành công
        String otp;
        try {
            otp = authService.registerPatient(email, password, fullName.trim(), phone.trim(),
                    dateOfBirth, gender, address, emergencyContactName, emergencyContactPhone);
        } catch (Exception ex) {
            log.error("Lỗi đăng ký tài khoản cho email={}: {}", email, ex.getMessage(), ex);
            preserveFormData(model, email, fullName, phone, dateOfBirth, gender, address, emergencyContactName,
                    emergencyContactPhone);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi đăng ký: " + ex.getMessage());
            return "auth/register";
        }

        // ===== BƯỚC 2: Gửi email OTP (NGOÀI transaction, sau khi DB đã commit) =====
        // Nếu gửi email fail → Account/Patient/OTP vẫn đã lưu trong DB
        // User vẫn được redirect tới trang verify-otp và có thể nhấn "Gửi lại OTP"
        authService.sendOtpEmailSafely(email, otp, "REGISTRATION");

        // Chuyển tới trang xác thực OTP
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        return "redirect:/auth/verify-otp?email=" + encodedEmail + "&type=REGISTRATION";
    }

    // ==================== VERIFY OTP ====================

    @GetMapping("/verify-otp")
    public String verifyOtpPage(
            @RequestParam String email,
            @RequestParam(defaultValue = "REGISTRATION") String type,
            @RequestParam(required = false) Boolean triggerResend,
            Model model) {
        String trimmedEmail = (email != null) ? email.trim() : "";
        if (Boolean.TRUE.equals(triggerResend)) {
            try {
                if (!"PASSWORD_RESET".equals(type) || authService.emailExists(trimmedEmail)) {
                    // Tạo OTP mới trong DB (transactional)
                    String otp = authService.resendOtp(trimmedEmail, type);
                    // Gửi email ngoài transaction
                    authService.sendOtpEmailSafely(trimmedEmail, otp, type);
                }
                model.addAttribute("successMessage", "Mã OTP mới đã được gửi tới email của bạn!");
            } catch (Exception ex) {
                log.error("Lỗi tự động gửi lại OTP cho email={}: {}", trimmedEmail, ex.getMessage(), ex);
                model.addAttribute("errorMessage", "Không thể tự động gửi mã OTP mới: " + ex.getMessage());
            }
        }
        if ("PASSWORD_RESET".equals(type) && !model.containsAttribute("successMessage") && !model.containsAttribute("errorMessage")) {
            model.addAttribute("successMessage", "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi tới email đó.");
        }
        model.addAttribute("email", trimmedEmail);
        model.addAttribute("otpType", type);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam(defaultValue = "REGISTRATION") String type,
            HttpServletRequest request,
            Model model) {
        String trimmedEmail = (email != null) ? email.trim() : "";
        String error = authService.verifyOtp(trimmedEmail, otp, type);

        if (error != null) {
            model.addAttribute("email", trimmedEmail);
            model.addAttribute("otpType", type);
            model.addAttribute("errorMessage", error);
            return "auth/verify-otp";
        }

        if ("PASSWORD_RESET".equals(type)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("RESET_PASSWORD_EMAIL", trimmedEmail);
            log.info("Xác thực OTP thành công để đặt lại mật khẩu cho email={}", trimmedEmail);
            return "redirect:/auth/reset-password";
        }

        // Xác thực thành công — tự động đăng nhập luôn
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(trimmedEmail);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Lưu SecurityContext vào session để Spring Security nhận diện
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            log.info("Auto-login thành công sau xác thực OTP cho email={}", trimmedEmail);
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Không thể auto-login sau xác thực OTP cho email={}: {}", email, e.getMessage());
            return "redirect:/auth/login?verified=true";
        }
    }

    // ==================== RESEND OTP ====================

    @PostMapping("/resend-otp")
    public String resendOtp(
            @RequestParam String email,
            @RequestParam(defaultValue = "REGISTRATION") String type,
            Model model) {
        String trimmedEmail = (email != null) ? email.trim() : "";
        try {
            if (!"PASSWORD_RESET".equals(type) || authService.emailExists(trimmedEmail)) {
                // BƯỚC 1: Tạo OTP mới trong DB (transactional)
                String otp = authService.resendOtp(trimmedEmail, type);

                // BƯỚC 2: Gửi email (ngoài transaction, sau khi DB đã commit)
                authService.sendOtpEmailSafely(trimmedEmail, otp, type);
            }

            model.addAttribute("successMessage", "Mã OTP mới đã được gửi tới email của bạn!");
        } catch (Exception ex) {
            log.error("Lỗi gửi lại OTP cho email={}: {}", trimmedEmail, ex.getMessage(), ex);
            model.addAttribute("errorMessage", "Không thể gửi lại mã OTP: " + ex.getMessage());
        }
        model.addAttribute("email", trimmedEmail);
        model.addAttribute("otpType", type);
        return "auth/verify-otp";
    }

    // ==================== FORGOT PASSWORD ====================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestParam String email,
            Model model) {
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Email không được để trống!");
            return "auth/forgot-password";
        }
        String trimmedEmail = email.trim();
        if (!trimmedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            model.addAttribute("errorMessage", "Email không đúng định dạng!");
            model.addAttribute("email", email);
            return "auth/forgot-password";
        }

        try {
            if (authService.emailExists(trimmedEmail)) {
                // Tạo OTP cho forgot password
                String otp = authService.createOtpRecord(trimmedEmail, "PASSWORD_RESET");
                // Gửi email OTP
                authService.sendOtpEmailSafely(trimmedEmail, otp, "PASSWORD_RESET");
            } else {
                log.info("Yêu cầu quên mật khẩu cho email không tồn tại: {}", trimmedEmail);
            }
            
            String encodedEmail = URLEncoder.encode(trimmedEmail, StandardCharsets.UTF_8);
            return "redirect:/auth/verify-otp?email=" + encodedEmail + "&type=PASSWORD_RESET";
        } catch (Exception ex) {
            log.error("Lỗi khi xử lý quên mật khẩu cho email={}: {}", trimmedEmail, ex.getMessage(), ex);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi: " + ex.getMessage());
            model.addAttribute("email", trimmedEmail);
            return "auth/forgot-password";
        }
    }

    // ==================== RESET PASSWORD ====================

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("RESET_PASSWORD_EMAIL") == null) {
            return "redirect:/auth/forgot-password";
        }
        model.addAttribute("email", session.getAttribute("RESET_PASSWORD_EMAIL"));
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpServletRequest request,
            Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("RESET_PASSWORD_EMAIL") == null) {
            return "redirect:/auth/forgot-password";
        }
        String email = (String) session.getAttribute("RESET_PASSWORD_EMAIL");

        if (password.length() < 8) {
            model.addAttribute("email", email);
            model.addAttribute("errorMessage", "Mật khẩu phải có ít nhất 8 ký tự!");
            return "auth/reset-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("email", email);
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "auth/reset-password";
        }

        try {
            authService.resetPassword(email, password);
            session.removeAttribute("RESET_PASSWORD_EMAIL");
            return "redirect:/auth/login?resetSuccess=true";
        } catch (Exception ex) {
            log.error("Lỗi khi đặt lại mật khẩu cho email={}: {}", email, ex.getMessage(), ex);
            model.addAttribute("email", email);
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi đặt lại mật khẩu: " + ex.getMessage());
            return "auth/reset-password";
        }
    }

    private void preserveFormData(Model model, String email, String fullName, String phone,
            String dateOfBirth, String gender, String address,
            String emergencyContactName, String emergencyContactPhone) {
        model.addAttribute("email", email);
        model.addAttribute("fullName", fullName);
        model.addAttribute("phone", phone);
        model.addAttribute("dateOfBirth", dateOfBirth);
        model.addAttribute("gender", gender);
        model.addAttribute("address", address);
        model.addAttribute("emergencyContactName", emergencyContactName);
        model.addAttribute("emergencyContactPhone", emergencyContactPhone);
    }
}