package com.rpm.remotepatientmonitoring.auth;

import com.rpm.remotepatientmonitoring.controller.AuthController;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.service.AuthService;
import com.rpm.remotepatientmonitoring.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.rpm.remotepatientmonitoring.config.MockBeansConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MockBeansConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UserDetails sampleUserDetails;

    @BeforeEach
    void setUp() {
        sampleUserDetails = new User(
                "patient@example.com",
                "hashedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
        );
    }

    @Nested
    @DisplayName("GET Pages Rendering")
    class GetPages {

        @Test
        @DisplayName("GET /auth/login renders login template with appropriate error/success attributes")
        void getLogin_rendersView() throws Exception {
            mockMvc.perform(get("/auth/login")
                            .param("error", "true")
                            .param("username", "test@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attribute("errorMessage", "Email hoặc mật khẩu không đúng!"))
                    .andExpect(model().attribute("username", "test@example.com"));

            mockMvc.perform(get("/auth/login")
                            .param("disabled", "true")
                            .param("username", "unverified@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attribute("errorMessage", "Vui lòng xác thực email trước khi đăng nhập."))
                    .andExpect(model().attribute("emailNotVerified", true));

            mockMvc.perform(get("/auth/login")
                            .param("logout", "true"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("logoutMessage", "Đăng xuất thành công!"));

            mockMvc.perform(get("/auth/login")
                            .param("verified", "true"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("successMessage", "Xác thực email thành công! Bạn có thể đăng nhập."));
        }

        @Test
        @DisplayName("GET /auth/register renders register template")
        void getRegister_rendersView() throws Exception {
            mockMvc.perform(get("/auth/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"));
        }

        @Test
        @DisplayName("GET /auth/forgot-password renders forgot-password template")
        void getForgotPassword_rendersView() throws Exception {
            mockMvc.perform(get("/auth/forgot-password"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/forgot-password"));
        }

        @Test
        @DisplayName("GET /auth/reset-password redirects to forgot-password if no session email")
        void getResetPassword_withoutSession_redirectsToForgotPassword() throws Exception {
            mockMvc.perform(get("/auth/reset-password"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/forgot-password"));
        }

        @Test
        @DisplayName("GET /auth/reset-password renders view if session email exists")
        void getResetPassword_withSession_rendersView() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("RESET_PASSWORD_EMAIL", "test@example.com");

            mockMvc.perform(get("/auth/reset-password").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/reset-password"))
                    .andExpect(model().attribute("email", "test@example.com"));
        }
    }

    @Nested
    @DisplayName("Registration Endpoint Validation")
    class RegistrationValidation {

        @Test
        @DisplayName("POST /auth/register fails on blank or invalid full name")
        void postRegister_invalidFullName_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "Nguyen123") // Invalid: contains digits
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attributeExists("errorMessage"));
        }

        @Test
        @DisplayName("POST /auth/register fails on invalid phone number format")
        void postRegister_invalidPhone_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "12345") // Invalid phone
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Số điện thoại không đúng định dạng!"));
        }

        @Test
        @DisplayName("POST /auth/register fails on future date of birth")
        void postRegister_futureDob_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "2099-01-01") // Future date
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Ngày sinh không được ở tương lai!"));
        }

        @Test
        @DisplayName("POST /auth/register fails if password and confirmPassword do not match")
        void postRegister_passwordMismatch_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password456")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Mật khẩu xác nhận không khớp!"));
        }

        @Test
        @DisplayName("POST /auth/register fails if email is already registered")
        void postRegister_duplicateEmail_returnsRegisterViewWithError() throws Exception {
            when(authService.emailExists("test@example.com")).thenReturn(true);

            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Email này đã được đăng ký!"));
        }

        @Test
        @DisplayName("POST /auth/register fails when full name length is less than 2 characters")
        void postRegister_fullNameTooShort_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "A")
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Họ và tên chỉ được chứa chữ cái, khoảng trắng và độ dài từ 2 đến 50 ký tự!"));
        }

        @Test
        @DisplayName("POST /auth/register fails when full name length exceeds 50 characters")
        void postRegister_fullNameTooLong_returnsRegisterViewWithError() throws Exception {
            String longName = "Nguyen".repeat(10);
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", longName)
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Họ và tên chỉ được chứa chữ cái, khoảng trắng và độ dài từ 2 đến 50 ký tự!"));
        }

        @Test
        @DisplayName("POST /auth/register fails when emergency contact phone matches patient phone")
        void postRegister_emergencyPhoneMatchesPatientPhone_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "0912345678")
                            .param("emergencyContactPhone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Số điện thoại người liên hệ khẩn cấp không được trùng với số điện thoại của bạn!"));
        }

        @Test
        @DisplayName("POST /auth/register fails when date of birth makes age greater than 120")
        void postRegister_ageOver120_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1900-01-01")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Tuổi không được vượt quá 120!"));
        }

        @Test
        @DisplayName("POST /auth/register fails when email format is invalid")
        void postRegister_invalidEmailFormat_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "abc@")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Email không đúng định dạng!"));
        }

        @Test
        @DisplayName("POST /auth/register fails when password length is less than 8 characters")
        void postRegister_passwordLengthLessThan8_returnsRegisterViewWithError() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Pass123")
                            .param("confirmPassword", "Pass123")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("errorMessage", "Mật khẩu phải có ít nhất 8 ký tự!"));
        }

        @Test
        @DisplayName("POST /auth/register succeeds and redirects to verify-otp")
        void postRegister_validData_redirectsToVerifyOtp() throws Exception {
            when(authService.emailExists("test@example.com")).thenReturn(false);
            when(authService.phoneExists("0912345678")).thenReturn(false);
            when(authService.registerPatient(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), any()))
                    .thenReturn("123456");

            mockMvc.perform(post("/auth/register")
                            .param("email", "test@example.com")
                            .param("password", "Password123")
                            .param("confirmPassword", "Password123")
                            .param("fullName", "Nguyen Van A")
                            .param("phone", "0912345678")
                            .param("dateOfBirth", "1995-05-15")
                            .param("gender", "MALE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/auth/verify-otp?email=*&type=REGISTRATION"));

            verify(authService).sendOtpEmailSafely("test@example.com", "123456", "REGISTRATION");
        }
    }

    @Nested
    @DisplayName("OTP Verification Endpoint")
    class OtpVerificationEndpoints {

        @Test
        @DisplayName("POST /auth/verify-otp renders verify-otp template when verification returns error")
        void postVerifyOtp_failure_rendersViewWithError() throws Exception {
            when(authService.verifyOtp("test@example.com", "000000", "REGISTRATION"))
                    .thenReturn("Mã OTP không đúng hoặc đã được sử dụng!");

            mockMvc.perform(post("/auth/verify-otp")
                            .param("email", "test@example.com")
                            .param("otp", "000000")
                            .param("type", "REGISTRATION"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/verify-otp"))
                    .andExpect(model().attribute("errorMessage", "Mã OTP không đúng hoặc đã được sử dụng!"));
        }

        @Test
        @DisplayName("POST /auth/verify-otp REGISTRATION success auto-logins user and redirects to /dashboard")
        void postVerifyOtp_registrationSuccess_autoLoginsAndRedirectsToDashboard() throws Exception {
            when(authService.verifyOtp("test@example.com", "123456", "REGISTRATION")).thenReturn(null);
            when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(sampleUserDetails);

            mockMvc.perform(post("/auth/verify-otp")
                            .param("email", "test@example.com")
                            .param("otp", "123456")
                            .param("type", "REGISTRATION"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }

        @Test
        @DisplayName("POST /auth/verify-otp PASSWORD_RESET success sets session attribute and redirects to /auth/reset-password")
        void postVerifyOtp_passwordResetSuccess_redirectsToResetPassword() throws Exception {
            when(authService.verifyOtp("test@example.com", "654321", "PASSWORD_RESET")).thenReturn(null);

            mockMvc.perform(post("/auth/verify-otp")
                            .param("email", "test@example.com")
                            .param("otp", "654321")
                            .param("type", "PASSWORD_RESET"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/reset-password"));
        }
    }

    @Nested
    @DisplayName("Forgot & Reset Password Endpoints")
    class ForgotAndResetPassword {

        @Test
        @DisplayName("POST /auth/forgot-password with valid email sends OTP and redirects to verify-otp")
        void postForgotPassword_validEmail_sendsOtpAndRedirects() throws Exception {
            when(authService.emailExists("test@example.com")).thenReturn(true);
            when(authService.createOtpRecord("test@example.com", "PASSWORD_RESET")).thenReturn("654321");

            mockMvc.perform(post("/auth/forgot-password")
                            .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/auth/verify-otp?email=*&type=PASSWORD_RESET"));

            verify(authService).sendOtpEmailSafely("test@example.com", "654321", "PASSWORD_RESET");
        }

        @Test
        @DisplayName("POST /auth/forgot-password with non-existent email still redirects to verify-otp without revealing email status")
        void postForgotPassword_nonExistentEmail_redirectsToVerifyOtpNormally() throws Exception {
            when(authService.emailExists("nonexistent@example.com")).thenReturn(false);

            mockMvc.perform(post("/auth/forgot-password")
                            .param("email", "nonexistent@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/auth/verify-otp?email=*&type=PASSWORD_RESET"));

            verify(authService, never()).createOtpRecord(anyString(), anyString());
        }

        @Test
        @DisplayName("POST /auth/forgot-password with blank email returns error and does not send OTP")
        void postForgotPassword_blankEmail_returnsError() throws Exception {
            mockMvc.perform(post("/auth/forgot-password")
                            .param("email", "  "))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/forgot-password"))
                    .andExpect(model().attribute("errorMessage", "Email không được để trống!"));

            verify(authService, never()).createOtpRecord(anyString(), anyString());
        }

        @Test
        @DisplayName("POST /auth/reset-password validates passwords and calls resetPassword")
        void postResetPassword_validPassword_resetsPasswordAndRedirectsToLogin() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("RESET_PASSWORD_EMAIL", "test@example.com");

            mockMvc.perform(post("/auth/reset-password")
                            .session(session)
                            .param("password", "NewPassword123")
                            .param("confirmPassword", "NewPassword123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/login?resetSuccess=true"));

            verify(authService).resetPassword("test@example.com", "NewPassword123");
        }

        @Test
        @DisplayName("POST /auth/reset-password fails when new password length is less than 8 characters")
        void postResetPassword_passwordLengthLessThan8_returnsError() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("RESET_PASSWORD_EMAIL", "test@example.com");

            mockMvc.perform(post("/auth/reset-password")
                            .session(session)
                            .param("password", "Pass123")
                            .param("confirmPassword", "Pass123"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/reset-password"))
                    .andExpect(model().attribute("errorMessage", "Mật khẩu phải có ít nhất 8 ký tự!"));
        }

        @Test
        @DisplayName("POST /auth/reset-password fails when confirm password does not match")
        void postResetPassword_passwordsDoNotMatch_returnsError() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("RESET_PASSWORD_EMAIL", "test@example.com");

            mockMvc.perform(post("/auth/reset-password")
                            .session(session)
                            .param("password", "NewPassword123")
                            .param("confirmPassword", "DifferentPassword123"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/reset-password"))
                    .andExpect(model().attribute("errorMessage", "Mật khẩu xác nhận không khớp!"));
        }
    }
}
