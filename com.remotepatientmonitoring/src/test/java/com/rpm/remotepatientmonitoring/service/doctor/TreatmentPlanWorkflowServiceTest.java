package com.rpm.remotepatientmonitoring.service.doctor;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentPlanWorkflowServiceTest {

    @Mock
    private TreatmentPlanRepository treatmentPlanRepository;

    @Mock
    private NutritionRuleRepository nutritionRuleRepository;

    @Mock
    private PatientMedicationRepository patientMedicationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AuditTrailService auditTrailService;

    @Mock
    private ClinicalRecordRepository clinicalRecordRepository;

    @Mock
    private HealthLogRepository healthLogRepository;

    @Mock
    private com.rpm.remotepatientmonitoring.service.patient.PatientHealthService patientHealthService;

    @InjectMocks
    private TreatmentPlanWorkflowService treatmentPlanWorkflowService;

    @Test
    void createNewTreatmentPlan_TC01_FirstTimePlan_PatientStatusNew() {
        // Arrange (Không có phác đồ cũ, Bệnh nhân NEW)
        Patient patient = new Patient(); patient.setId(100); patient.setStatus("NEW");
        Doctor doctor = new Doctor(); doctor.setId(50);
        
        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(100, true)).thenReturn(Optional.empty());
        when(nutritionRuleRepository.findByPatientIdAndIsCurrent(100, true)).thenReturn(Optional.empty());
        when(patientMedicationRepository.findByPatientIdAndIsActiveTrue(100)).thenReturn(Collections.emptyList());
        
        NutritionRule savedRule = new NutritionRule();
        when(nutritionRuleRepository.save(any(NutritionRule.class))).thenReturn(savedRule);

        // Act
        treatmentPlanWorkflowService.createNewTreatmentPlan(
                patient, doctor, 
                120, 80, new BigDecimal("5.0"), new BigDecimal("6.0"), new BigDecimal("70.0"), new BigDecimal("170.0"), new BigDecimal("24.2"), // Baseline
                110, 70, new BigDecimal("4.5"), new BigDecimal("5.5"), new BigDecimal("68.0"), // Target
                "Order", "Exercise", "Notes", // Orders & Goals
                2000, new BigDecimal("200"), new BigDecimal("5"), new BigDecimal("30"), new BigDecimal("50"), new BigDecimal("100"), 2000, "NutriNotes", // Nutrition
                null, null, null // Mảng thuốc Null
        );

        // Assert
        verify(treatmentPlanRepository, times(1)).save(any(TreatmentPlan.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
        assertEquals("TREATING", patient.getStatus());
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void createNewTreatmentPlan_TC02_UpdateExistingPlan_WithMedications() {
        // Arrange (Có phác đồ cũ, Truyền vào danh sách thuốc)
        Patient patient = new Patient(); patient.setId(100); patient.setStatus("TREATING");
        Doctor doctor = new Doctor(); doctor.setId(50);
        
        TreatmentPlan oldPlan = new TreatmentPlan(); oldPlan.setIsCurrent(true);
        when(treatmentPlanRepository.findByPatientIdAndIsCurrent(100, true)).thenReturn(Optional.of(oldPlan));
        
        NutritionRule oldRule = new NutritionRule(); oldRule.setIsCurrent(true);
        when(nutritionRuleRepository.findByPatientIdAndIsCurrent(100, true)).thenReturn(Optional.of(oldRule));
        
        PatientMedication oldMed = new PatientMedication(); oldMed.setIsActive(true);
        when(patientMedicationRepository.findByPatientIdAndIsActiveTrue(100)).thenReturn(Collections.singletonList(oldMed));

        // Act
        treatmentPlanWorkflowService.createNewTreatmentPlan(
                patient, doctor, 
                120, 80, new BigDecimal("5.0"), new BigDecimal("6.0"), new BigDecimal("70.0"), new BigDecimal("170.0"), new BigDecimal("24.2"), // Baseline
                110, 70, new BigDecimal("4.5"), new BigDecimal("5.5"), new BigDecimal("68.0"), // Target
                "Order", "Exercise", "Notes", // Orders & Goals
                2000, new BigDecimal("200"), new BigDecimal("5"), new BigDecimal("30"), new BigDecimal("50"), new BigDecimal("100"), 2000, "NutriNotes", // Nutrition
                Arrays.asList("Paracetamol", ""), Arrays.asList("500mg"), Arrays.asList("MORNING") // Thuốc
        );

        // Assert (Kiểm tra việc vô hiệu hóa cũ)
        assertFalse(oldPlan.getIsCurrent());
        assertFalse(oldRule.getIsCurrent());
        assertFalse(oldMed.getIsActive());
        
        verify(treatmentPlanRepository, times(2)).save(any(TreatmentPlan.class)); // 1 update cũ + 1 save mới
        
        // Truyền 2 thuốc nhưng 1 thuốc bị rỗng "" -> chỉ tạo 1 thuốc hợp lệ
        verify(patientMedicationRepository, times(1)).save(any(PatientMedication.class));
        
        // Không đổi status bệnh nhân vì đã là TREATING
        verify(patientRepository, never()).save(patient);
    }
}
