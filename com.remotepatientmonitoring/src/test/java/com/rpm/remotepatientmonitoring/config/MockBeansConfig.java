package com.rpm.remotepatientmonitoring.config;

import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.*;
import com.rpm.remotepatientmonitoring.service.doctor.*;
import com.rpm.remotepatientmonitoring.service.hospital.*;
import com.rpm.remotepatientmonitoring.service.patient.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class MockBeansConfig {

    @MockBean public AccountRepository accountRepository;
    @MockBean public PatientRepository patientRepository;
    @MockBean public DoctorRepository doctorRepository;
    @MockBean public HospitalRepository hospitalRepository;
    @MockBean public OtpCodeRepository otpCodeRepository;
    @MockBean public BugReportRepository bugReportRepository;
    @MockBean public AiChatHistoryRepository aiChatHistoryRepository;
    @MockBean public HealthLogRepository healthLogRepository;
    @MockBean public WaterLogRepository waterLogRepository;
    @MockBean public ExerciseLogRepository exerciseLogRepository;
    @MockBean public AppointmentRepository appointmentRepository;
    @MockBean public AlertRepository alertRepository;
    @MockBean public AlertThresholdRepository alertThresholdRepository;
    @MockBean public AuditTrailRepository auditTrailRepository;
    @MockBean public ClinicalRecordRepository clinicalRecordRepository;
    @MockBean public DiseaseProfileRepository diseaseProfileRepository;
    @MockBean public DoctorRatingRepository doctorRatingRepository;
    @MockBean public EmergencyGuideRepository emergencyGuideRepository;
    @MockBean public EmergencyProtocolRepository emergencyProtocolRepository;
    @MockBean public ExerciseGuidelineRepository exerciseGuidelineRepository;
    @MockBean public FoodDictionaryRepository foodDictionaryRepository;
    @MockBean public HospitalAdminRepository hospitalAdminRepository;
    @MockBean public MedicationLogRepository medicationLogRepository;
    @MockBean public NotificationRepository notificationRepository;
    @MockBean public NutritionRuleRepository nutritionRuleRepository;
    @MockBean public PatientExerciseRepository patientExerciseRepository;
    @MockBean public PatientMealRepository patientMealRepository;
    @MockBean public PatientMedicationRepository patientMedicationRepository;
    @MockBean public SystemLogRepository systemLogRepository;
    @MockBean public TreatmentPlanRepository treatmentPlanRepository;
    @MockBean public ChangeRequestRepository changeRequestRepository;
    @MockBean public AppRatingRepository appRatingRepository;
    @MockBean public AiClinicalSummaryRepository aiClinicalSummaryRepository;

    @MockBean public AiChatService aiChatService;
    @MockBean public BugReportService bugReportService;
    @MockBean public EmailService emailService;
    @MockBean public AuditTrailService auditTrailService;
    @MockBean public DoctorService doctorService;
    @MockBean public ExcelExportService excelExportService;
    @MockBean public HospitalConfigService hospitalConfigService;
    @MockBean public HospitalProfileService hospitalProfileService;
    @MockBean public ExerciseLogService exerciseLogService;
    @MockBean public PatientHealthService patientHealthService;
    @MockBean public PatientInteractionService patientInteractionService;
    @MockBean public PatientService patientService;
    @MockBean public org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockBean public DoctorPatientService doctorPatientService;
    @MockBean public TreatmentPlanWorkflowService treatmentPlanWorkflowService;
    @MockBean public RatingService ratingService;

    @org.springframework.context.annotation.Bean
    public org.springframework.web.servlet.ViewResolver viewResolver() {
        org.springframework.web.servlet.view.InternalResourceViewResolver resolver = new org.springframework.web.servlet.view.InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }
}
