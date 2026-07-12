package com.rpm.remotepatientmonitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.model.OtpCode;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import com.rpm.remotepatientmonitoring.repository.OtpCodeRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

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

    @InjectMocks
    private AuthService authService;

    private Hospital hospital;

    @BeforeEach
    void setUp() {
        hospital = new Hospital();
        hospital.setId(1);
        hospital.setFullName("Hospital A");
    }

    @Test
    void testEmailExists_Verified() {
        Account verifiedAccount = Account.builder().email("test@example.com").isEmailVerified(true).build();
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(verifiedAccount));

        assertTrue(authService.emailExists("test@example.com"));
    }

    @Test
    void testEmailExists_Unverified() {
        Account unverifiedAccount = Account.builder().email("test@example.com").isEmailVerified(false).build();
        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(unverifiedAccount));

        assertFalse(authService.emailExists("test@example.com"));
    }

    @Test
    void testRegisterPatient_NewAccount() {
        when(accountRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        
        Account savedAccount = Account.builder().id(123).email("new@example.com").isEmailVerified(false).build();
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        String otp = authService.registerPatient(
                "new@example.com", "password123", "John Doe", "0987654321",
                "1990-01-01", "Male", "Address", "Contact Name", "Contact Phone"
        );

        assertNotNull(otp);
        assertEquals(6, otp.length());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account capturedAccount = accountCaptor.getValue();
        assertEquals("new@example.com", capturedAccount.getEmail());
        assertFalse(capturedAccount.getIsEmailVerified());
        assertNotNull(capturedAccount.getRegistrationDetails());
        assertTrue(capturedAccount.getRegistrationDetails().contains("John Doe"));

        // Verify patient is NOT created during registration
        verify(patientRepository, never()).save(any(Patient.class));
        verify(otpCodeRepository).save(any(OtpCode.class));
    }

    @Test
    void testVerifyOtp_Success() throws Exception {
        String email = "test@example.com";
        String rawOtp = "123456";
        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .otpCode(rawOtp)
                .otpType("REGISTRATION")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .build();

        when(otpCodeRepository.findByEmailAndOtpCodeAndOtpTypeAndIsUsedFalse(email, rawOtp, "REGISTRATION"))
                .thenReturn(Optional.of(otpCode));

        Map<String, String> detailsMap = new HashMap<>();
        detailsMap.put("fullName", "John Doe");
        detailsMap.put("phone", "0987654321");
        detailsMap.put("dateOfBirth", "1990-01-01");
        detailsMap.put("gender", "Male");
        detailsMap.put("address", "Address");
        detailsMap.put("emergencyContactName", "Contact Name");
        detailsMap.put("emergencyContactPhone", "Contact Phone");
        String jsonDetails = objectMapper.writeValueAsString(detailsMap);

        Account account = Account.builder()
                .id(1)
                .email(email)
                .isEmailVerified(false)
                .registrationDetails(jsonDetails)
                .build();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(hospitalRepository.findAll()).thenReturn(Collections.singletonList(hospital));

        String error = authService.verifyOtp(email, rawOtp, "REGISTRATION");

        assertNull(error); // null means success
        assertTrue(otpCode.getIsUsed());
        assertTrue(account.getIsEmailVerified());
        assertNull(account.getRegistrationDetails());

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(patientCaptor.capture());
        Patient createdPatient = patientCaptor.getValue();
        assertEquals("John Doe", createdPatient.getFullName());
        assertEquals("0987654321", createdPatient.getPhone());
        assertEquals(LocalDate.parse("1990-01-01"), createdPatient.getDateOfBirth());
        assertEquals("Male", createdPatient.getGender());
        assertEquals("Address", createdPatient.getAddress());
        assertEquals("NEW", createdPatient.getStatus());
    }

    @Test
    void testVerifyOtp_Expired() {
        String email = "test@example.com";
        String rawOtp = "123456";
        OtpCode expiredOtp = OtpCode.builder()
                .email(email)
                .otpCode(rawOtp)
                .otpType("REGISTRATION")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .isUsed(false)
                .build();

        when(otpCodeRepository.findByEmailAndOtpCodeAndOtpTypeAndIsUsedFalse(email, rawOtp, "REGISTRATION"))
                .thenReturn(Optional.of(expiredOtp));

        String error = authService.verifyOtp(email, rawOtp, "REGISTRATION");

        assertNotNull(error);
        assertTrue(error.contains("hết hạn"));
        assertTrue(expiredOtp.getIsUsed());
        verify(patientRepository, never()).save(any(Patient.class));
    }
}
