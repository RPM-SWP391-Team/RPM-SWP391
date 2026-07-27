package com.rpm.remotepatientmonitoring.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.model.OtpCode;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import com.rpm.remotepatientmonitoring.repository.OtpCodeRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.service.AuthService;
import com.rpm.remotepatientmonitoring.service.EmailService;
import com.rpm.remotepatientmonitoring.service.doctor.AuditTrailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuditTrailService auditTrailService;

    @InjectMocks
    private AuthService authService;

    private Account sampleAccount;
    private Hospital sampleHospital;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id(1)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role("PATIENT")
                .isEmailVerified(false)
                .isActive(true)
                .build();

        sampleHospital = Hospital.builder()
                .id(100)
                .fullName("General Hospital")
                .build();
    }

    @Nested
    @DisplayName("Email & Phone Existence Checks")
    class ExistenceChecks {

        @Test
        @DisplayName("emailExists returns true only if account exists AND is email verified")
        void emailExists_verifiedAccount_returnsTrue() {
            sampleAccount.setIsEmailVerified(true);
            when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleAccount));

            assertTrue(authService.emailExists("test@example.com"));
        }

        @Test
        @DisplayName("emailExists returns false if account exists but email is NOT verified")
        void emailExists_unverifiedAccount_returnsFalse() {
            sampleAccount.setIsEmailVerified(false);
            when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleAccount));

            assertFalse(authService.emailExists("test@example.com"));
        }

        @Test
        @DisplayName("emailExists returns false if account does not exist")
        void emailExists_accountNotFound_returnsFalse() {
            when(accountRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            assertFalse(authService.emailExists("nonexistent@example.com"));
        }

        @Test
        @DisplayName("phoneExists returns true if phone exists in patientRepository")
        void phoneExists_found_returnsTrue() {
            when(patientRepository.findByPhone("0987654321")).thenReturn(Optional.of(new Patient()));

            assertTrue(authService.phoneExists("0987654321"));
        }

        @Test
        @DisplayName("phoneExists returns false if phone is not found")
        void phoneExists_notFound_returnsFalse() {
            when(patientRepository.findByPhone("0987654321")).thenReturn(Optional.empty());

            assertFalse(authService.phoneExists("0987654321"));
        }
    }

    @Nested
    @DisplayName("Patient Registration")
    class PatientRegistration {

        @Test
        @DisplayName("registerPatient creates new Account and saves registrationDetails and creates OTP record")
        void registerPatient_newAccount_success() {
            when(accountRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
            when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
                Account acc = invocation.getArgument(0);
                acc.setId(1);
                return acc;
            });
            when(otpCodeRepository.findByEmailAndOtpTypeAndIsUsedFalse("new@example.com", "REGISTRATION"))
                    .thenReturn(Collections.emptyList());

            String otp = authService.registerPatient(
                    "new@example.com", "Password123", "Nguyen Van A", "0912345678",
                    "1995-05-15", "MALE", "123 Main St", "Nguyen Van B", "0987654321"
            );

            assertNotNull(otp);
            assertEquals(6, otp.length());

            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());
            Account savedAccount = accountCaptor.getValue();

            assertEquals("new@example.com", savedAccount.getEmail());
            assertEquals("encodedPassword", savedAccount.getPasswordHash());
            assertEquals("PATIENT", savedAccount.getRole());
            assertFalse(savedAccount.getIsEmailVerified());
            assertNotNull(savedAccount.getRegistrationDetails());

            verify(otpCodeRepository).save(any(OtpCode.class));
        }

        @Test
        @DisplayName("registerPatient throws IllegalStateException if email is already verified")
        void registerPatient_alreadyVerified_throwsException() {
            sampleAccount.setIsEmailVerified(true);
            when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleAccount));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    authService.registerPatient(
                            "test@example.com", "Password123", "Nguyen Van A", "0912345678",
                            "1995-05-15", "MALE", null, null, null
                    ));

            assertTrue(ex.getMessage().contains("đã được xác thực"));
        }
    }

    @Nested
    @DisplayName("OTP Management")
    class OtpManagement {

        @Test
        @DisplayName("createOtpRecord invalidates previous unused OTPs and creates new 6-digit OTP")
        void createOtpRecord_invalidatesOldOtps_createsNew() {
            OtpCode oldOtp = OtpCode.builder()
                    .id(10)
                    .email("test@example.com")
                    .otpType("REGISTRATION")
                    .isUsed(false)
                    .build();

            when(otpCodeRepository.findByEmailAndOtpTypeAndIsUsedFalse("test@example.com", "REGISTRATION"))
                    .thenReturn(List.of(oldOtp));

            String otp = authService.createOtpRecord("test@example.com", "REGISTRATION");

            assertNotNull(otp);
            assertEquals(6, otp.length());
            assertTrue(oldOtp.getIsUsed());
            verify(otpCodeRepository).saveAll(List.of(oldOtp));
            verify(otpCodeRepository).save(any(OtpCode.class));
        }

        @Test
        @DisplayName("resendOtp invalidates previous unused OTPs and creates new 6-digit OTP")
        void resendOtp_invalidatesOldOtps_createsNew() {
            OtpCode oldOtp = OtpCode.builder()
                    .id(10)
                    .email("test@example.com")
                    .otpType("REGISTRATION")
                    .isUsed(false)
                    .build();

            when(otpCodeRepository.findByEmailAndOtpTypeAndIsUsedFalse("test@example.com", "REGISTRATION"))
                    .thenReturn(List.of(oldOtp));

            String newOtp = authService.resendOtp("test@example.com", "REGISTRATION");

            assertNotNull(newOtp);
            assertEquals(6, newOtp.length());
            assertTrue(oldOtp.getIsUsed());
            verify(otpCodeRepository).saveAll(List.of(oldOtp));
            verify(otpCodeRepository).save(any(OtpCode.class));
        }

        @Test
        @DisplayName("sendOtpEmailSafely catches exception without rethrowing")
        void sendOtpEmailSafely_catchesExceptions() {
            doThrow(new RuntimeException("Mail server down"))
                    .when(emailService).sendOtpEmail(anyString(), anyString(), anyString());

            assertDoesNotThrow(() -> authService.sendOtpEmailSafely("test@example.com", "123456", "REGISTRATION"));
        }
    }

    @Nested
    @DisplayName("OTP Verification")
    class OtpVerification {

        @Test
        @DisplayName("verifyOtp returns error message if OTP code is incorrect or not found")
        void verifyOtp_notFound_returnsError() {
            when(otpCodeRepository.findByEmailAndOtpCodeAndOtpTypeAndIsUsedFalse("test@example.com", "000000", "REGISTRATION"))
                    .thenReturn(Optional.empty());

            String result = authService.verifyOtp("test@example.com", "000000", "REGISTRATION");

            assertEquals("Mã OTP không đúng hoặc đã được sử dụng!", result);
        }

        @Test
        @DisplayName("verifyOtp marks expired OTP as used and returns expiration error message")
        void verifyOtp_expired_returnsError() {
            OtpCode expiredOtp = OtpCode.builder()
                    .id(1)
                    .email("test@example.com")
                    .otpCode("123456")
                    .otpType("REGISTRATION")
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .isUsed(false)
                    .build();

            when(otpCodeRepository.findByEmailAndOtpCodeAndOtpTypeAndIsUsedFalse("test@example.com", "123456", "REGISTRATION"))
                    .thenReturn(Optional.of(expiredOtp));

            String result = authService.verifyOtp("test@example.com", "123456", "REGISTRATION");

            assertEquals("Mã OTP đã hết hạn! Vui lòng yêu cầu gửi lại.", result);
            assertTrue(expiredOtp.getIsUsed());
            verify(otpCodeRepository).save(expiredOtp);
        }

        @Test
        @DisplayName("verifyOtp REGISTRATION success updates Account to verified and creates Patient entity")
        void verifyOtp_registrationSuccess_createsPatientAndVerifiesAccount() {
            OtpCode validOtp = OtpCode.builder()
                    .id(1)
                    .email("test@example.com")
                    .otpCode("123456")
                    .otpType("REGISTRATION")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .isUsed(false)
                    .build();

            sampleAccount.setRegistrationDetails("{" +
                    "\"fullName\":\"Nguyen Van A\"," +
                    "\"phone\":\"0912345678\"," +
                    "\"dateOfBirth\":\"1995-05-15\"," +
                    "\"gender\":\"MALE\"," +
                    "\"address\":\"123 Main St\"," +
                    "\"emergencyContactName\":\"Nguyen Van B\"," +
                    "\"emergencyContactPhone\":\"0987654321\"" +
                    "}");

            when(otpCodeRepository.findByEmailAndOtpCodeAndOtpTypeAndIsUsedFalse("test@example.com", "123456", "REGISTRATION"))
                    .thenReturn(Optional.of(validOtp));
            when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleAccount));
            when(hospitalRepository.findAll()).thenReturn(List.of(sampleHospital));

            String result = authService.verifyOtp("test@example.com", "123456", "REGISTRATION");

            assertNull(result);
            assertTrue(validOtp.getIsUsed());
            assertTrue(sampleAccount.getIsEmailVerified());
            assertNull(sampleAccount.getRegistrationDetails());

            ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
            verify(patientRepository).save(patientCaptor.capture());
            Patient savedPatient = patientCaptor.getValue();

            assertEquals("Nguyen Van A", savedPatient.getFullName());
            assertEquals("0912345678", savedPatient.getPhone());
            assertEquals(LocalDate.parse("1995-05-15"), savedPatient.getDateOfBirth());
            assertEquals("MALE", savedPatient.getGender());
            assertEquals("NEW", savedPatient.getStatus());
            assertEquals("ONLINE", savedPatient.getRegistrationSource());
            assertTrue(savedPatient.getIsActive());
        }

        @Test
        @DisplayName("verifyOtp PASSWORD_RESET success marks OTP used and returns null")
        void verifyOtp_passwordResetSuccess_returnsNull() {
            OtpCode validOtp = OtpCode.builder()
                    .id(2)
                    .email("test@example.com")
                    .otpCode("654321")
                    .otpType("PASSWORD_RESET")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .isUsed(false)
                    .build();

            when(otpCodeRepository.findByEmailAndOtpCodeAndOtpTypeAndIsUsedFalse("test@example.com", "654321", "PASSWORD_RESET"))
                    .thenReturn(Optional.of(validOtp));

            String result = authService.verifyOtp("test@example.com", "654321", "PASSWORD_RESET");

            assertNull(result);
            assertTrue(validOtp.getIsUsed());
            verify(otpCodeRepository).save(validOtp);
            verifyNoInteractions(patientRepository);
        }
    }

    @Nested
    @DisplayName("Password Reset")
    class PasswordReset {

        @Test
        @DisplayName("resetPassword throws IllegalArgumentException if email not found")
        void resetPassword_accountNotFound_throwsException() {
            when(accountRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    authService.resetPassword("nonexistent@example.com", "NewPassword123"));

            assertTrue(ex.getMessage().contains("Không tìm thấy tài khoản"));
        }

        @Test
        @DisplayName("resetPassword updates password hash and timestamps successfully")
        void resetPassword_success() {
            when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleAccount));
            when(passwordEncoder.encode("NewPassword123")).thenReturn("newHashedPassword");

            authService.resetPassword("test@example.com", "NewPassword123");

            assertEquals("newHashedPassword", sampleAccount.getPasswordHash());
            assertNotNull(sampleAccount.getUpdatedAt());
            verify(accountRepository).save(sampleAccount);
        }
    }
}
