package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.service.hospital.HospitalConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HospitalConfigServiceTest2 {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Mock
    private AlertThresholdRepository alertThresholdRepository;

    @Mock
    private EmergencyGuideRepository emergencyGuideRepository;

    @Mock
    private EmergencyProtocolRepository emergencyProtocolRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private ExerciseGuidelineRepository exerciseGuidelineRepository;

    @Mock
    private FoodDictionaryRepository foodDictionaryRepository;

    @Mock
    private PatientMealRepository patientMealRepository;

    @Mock
    private DiseaseProfileRepository diseaseProfileRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private HospitalConfigService hospitalConfigService;

    private Hospital hospital;
    private AlertThreshold threshold;

    @BeforeEach
    public void setUp() {
        hospital = new Hospital();
        hospital.setId(1);
        hospital.setFullName("Bệnh viện Đa khoa Mẫu");

        threshold = new AlertThreshold();
        threshold.setId(10);
        threshold.setHospital(hospital);
        threshold.setScope("HOSPITAL");
        threshold.setGlucoseHypoThreshold(BigDecimal.valueOf(4.4));
        threshold.setGlucoseNormalMax(BigDecimal.valueOf(10.0));
        threshold.setGlucoseHighMax(BigDecimal.valueOf(16.0));
        threshold.setSystolicNormalMax(120);
        threshold.setSystolicWarningMin(130);
        threshold.setSystolicWarningMax(139);
        threshold.setSystolicDangerMin(140);
        threshold.setSystolicDangerMax(179);
        threshold.setSystolicEmergencyThreshold(180);
        threshold.setDiastolicNormalMax(80);
        threshold.setDiastolicWarningMin(85);
        threshold.setDiastolicWarningMax(89);
        threshold.setDiastolicDangerMin(90);
        threshold.setDiastolicDangerMax(109);
        threshold.setDiastolicEmergencyThreshold(110);
    }

    // =========================================================================
    // TESTS FOR getGlobalThreshold & updateGlobalThreshold
    // =========================================================================

    @Test
    public void testGetGlobalThreshold_Exists_ReturnsThreshold() {
        // Arrange
        when(alertThresholdRepository.findByHospitalIdAndScope(1, "HOSPITAL")).thenReturn(Optional.of(threshold));

        // Act
        AlertThreshold result = hospitalConfigService.getGlobalThreshold(1);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getId());
        verify(alertThresholdRepository, never()).save(any(AlertThreshold.class));
    }

    @Test
    public void testGetGlobalThreshold_NotExists_SavesAndReturnsDefault() {
        // Arrange
        when(alertThresholdRepository.findByHospitalIdAndScope(1, "HOSPITAL")).thenReturn(Optional.empty());
        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));
        when(alertThresholdRepository.save(any(AlertThreshold.class))).thenAnswer(org.mockito.AdditionalAnswers.returnsFirstArg());

        // Act
        AlertThreshold result = hospitalConfigService.getGlobalThreshold(1);

        // Assert
        assertNotNull(result);
        assertEquals("HOSPITAL", result.getScope());
        assertEquals(120, result.getSystolicNormalMax());
        verify(alertThresholdRepository).save(any(AlertThreshold.class));
    }

    @Test
    public void testGetGlobalThreshold_HospitalNotFound_ThrowsException() {
        // Arrange
        when(alertThresholdRepository.findByHospitalIdAndScope(99, "HOSPITAL")).thenReturn(Optional.empty());
        when(hospitalRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        try {
            hospitalConfigService.getGlobalThreshold(99);
            fail("Nên ném ngoại lệ khi không tìm thấy bệnh viện");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Không tìm thấy bệnh viện với ID: 99"));
        }
    }

    @Test
    public void testUpdateGlobalThreshold_ValidData_SavesSuccessfully() {
        // Arrange
        AlertThreshold updated = new AlertThreshold();
        updated.setGlucoseHypoThreshold(BigDecimal.valueOf(4.0));
        updated.setGlucoseNormalMax(BigDecimal.valueOf(9.0));
        updated.setGlucoseHighMax(BigDecimal.valueOf(15.0));
        updated.setSystolicNormalMax(115);
        updated.setSystolicWarningMin(130);
        updated.setSystolicWarningMax(135);
        updated.setSystolicDangerMin(140);
        updated.setSystolicDangerMax(170);
        updated.setSystolicEmergencyThreshold(180);
        updated.setDiastolicNormalMax(75);
        updated.setDiastolicWarningMin(85);
        updated.setDiastolicWarningMax(88);
        updated.setDiastolicDangerMin(90);
        updated.setDiastolicDangerMax(105);
        updated.setDiastolicEmergencyThreshold(110);

        when(alertThresholdRepository.findByHospitalIdAndScope(1, "HOSPITAL")).thenReturn(Optional.of(threshold));
        when(alertThresholdRepository.save(any(AlertThreshold.class))).thenAnswer(org.mockito.AdditionalAnswers.returnsFirstArg());

        // Act
        AlertThreshold result = hospitalConfigService.updateGlobalThreshold(1, updated);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(4.0), result.getGlucoseHypoThreshold());
        assertEquals(115, result.getSystolicNormalMax());
        org.mockito.ArgumentCaptor<AlertThreshold> captor = org.mockito.ArgumentCaptor.forClass(AlertThreshold.class);
        verify(alertThresholdRepository).save(captor.capture());
        assertEquals(115, captor.getValue().getSystolicNormalMax());
    }

    @Test
    public void testUpdateGlobalThreshold_InvalidGlucoseRange_ThrowsException() {
        // Arrange
        AlertThreshold updated = new AlertThreshold();
        updated.setGlucoseHypoThreshold(BigDecimal.valueOf(10.0)); // > normal max
        updated.setGlucoseNormalMax(BigDecimal.valueOf(9.0));
        updated.setGlucoseHighMax(BigDecimal.valueOf(15.0));

        when(alertThresholdRepository.findByHospitalIdAndScope(1, "HOSPITAL")).thenReturn(Optional.of(threshold));

        // Act & Assert
        try {
            hospitalConfigService.updateGlobalThreshold(1, updated);
            fail("Nên ném ngoại lệ khi mốc hạ đường huyết lớn hơn mốc bình thường");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Mốc hạ đường huyết phải nhỏ hơn mốc bình thường tối đa") 
                    || e.getMessage().contains("Ngưỡng hạ đường huyết (Level 1) không được lớn hơn 4.4 mmol/L"));
        }
    }

    // =========================================================================
    // TESTS FOR EmergencyGuide
    // =========================================================================

    @Test
    public void testAddEmergencyGuide_Valid_SavesSuccessfully() {
        // Arrange
        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));
        when(emergencyGuideRepository.existsByHospitalIdAndAlertLevelAndMetricTypeAndIsActiveTrue(1, "RED", "GLUCOSE")).thenReturn(false);
        when(emergencyGuideRepository.save(any(EmergencyGuide.class))).thenAnswer(org.mockito.AdditionalAnswers.returnsFirstArg());

        // Act
        EmergencyGuide result = hospitalConfigService.addEmergencyGuide(1, "RED", "GLUCOSE", "Huong dan tieu duong", "Noi dung huong dan tieu duong chi tiet");

        // Assert
        assertNotNull(result);
        assertEquals("RED", result.getAlertLevel());
        assertEquals("Huong dan tieu duong", result.getTitle());
        verify(emergencyGuideRepository).save(any(EmergencyGuide.class));
    }

    @Test
    public void testAddEmergencyGuide_DuplicateGuide_ThrowsException() {
        // Arrange
        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));
        when(emergencyGuideRepository.existsByHospitalIdAndAlertLevelAndMetricTypeAndIsActiveTrue(1, "RED", "GLUCOSE")).thenReturn(true);

        // Act & Assert
        try {
            hospitalConfigService.addEmergencyGuide(1, "RED", "GLUCOSE", "Huong dan tieu duong", "Noi dung huong dan tieu duong chi tiet");
            fail("Nên ném ngoại lệ khi trùng hướng dẫn đang hoạt động");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("đã có hướng dẫn xử lý khẩn cấp"));
        }
    }

    @Test
    public void testEditEmergencyGuideContent_TooShortTitle_ThrowsException() {
        // Act & Assert
        try {
            hospitalConfigService.editEmergencyGuideContent(1, "Short", "Noi dung du dai tren 20 ky tu");
            fail("Nên ném ngoại lệ khi tiêu đề quá ngắn");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Tiêu đề phải từ 10 đến 50 ký tự"));
        }
    }

    // =========================================================================
    // TESTS FOR EmergencyProtocol
    // =========================================================================

    @Test
    public void testAddEmergencyProtocol_Valid_SavesSuccessfully() {
        // Arrange
        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));
        when(emergencyProtocolRepository.existsByHospitalIdAndConditionTypeAndIsActiveTrue(1, "DIABETES")).thenReturn(false);
        when(emergencyProtocolRepository.save(any(EmergencyProtocol.class))).thenAnswer(org.mockito.AdditionalAnswers.returnsFirstArg());

        // Act
        EmergencyProtocol result = hospitalConfigService.addEmergencyProtocol(1, "DIABETES", "Cam nang tieu duong", "Dau hieu tieu duong", "Noi dung chi dan cam nang");

        // Assert
        assertNotNull(result);
        assertEquals("Cam nang tieu duong", result.getTitle());
        verify(emergencyProtocolRepository).save(any(EmergencyProtocol.class));
    }

    @Test
    public void testAddEmergencyProtocol_DuplicateCondition_ThrowsException() {
        // Arrange
        when(hospitalRepository.findById(1)).thenReturn(Optional.of(hospital));
        when(emergencyProtocolRepository.existsByHospitalIdAndConditionTypeAndIsActiveTrue(1, "DIABETES")).thenReturn(true);

        // Act & Assert
        try {
            hospitalConfigService.addEmergencyProtocol(1, "DIABETES", "Cam nang tieu duong", "Dau hieu tieu duong", "Noi dung chi dan cam nang");
            fail("Nên ném ngoại lệ khi nhóm bệnh đã có cẩm nang");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("đã có cẩm nang đang hoạt động"));
        }
    }

    // =========================================================================
    // TESTS FOR delete & restore methods
    // =========================================================================

    @Test
    public void testDeleteEmergencyGuide_Valid_DeactivatesSuccessfully() {
        // Arrange
        EmergencyGuide guide = new EmergencyGuide();
        guide.setId(20);
        guide.setIsActive(true);

        when(emergencyGuideRepository.findById(20)).thenReturn(Optional.of(guide));

        // Act
        hospitalConfigService.deleteEmergencyGuide(20);

        // Assert
        assertFalse(guide.getIsActive());
        verify(emergencyGuideRepository).save(guide);
    }

    @Test
    public void testDeleteEmergencyProtocol_Valid_DeactivatesSuccessfully() {
        // Arrange
        EmergencyProtocol protocol = new EmergencyProtocol();
        protocol.setId(30);
        protocol.setIsActive(true);

        when(emergencyProtocolRepository.findById(30)).thenReturn(Optional.of(protocol));

        // Act
        hospitalConfigService.deleteEmergencyProtocol(30);

        // Assert
        assertFalse(protocol.getIsActive());
        verify(emergencyProtocolRepository).save(protocol);
    }

    @Test
    public void testRestoreEmergencyGuide_ConflictActiveGuide_ThrowsException() {
        // Arrange
        EmergencyGuide guide = new EmergencyGuide();
        guide.setId(20);
        guide.setHospital(hospital);
        guide.setAlertLevel("RED");
        guide.setMetricType("GLUCOSE");
        guide.setIsActive(false);

        when(emergencyGuideRepository.findById(20)).thenReturn(Optional.of(guide));
        when(emergencyGuideRepository.existsByHospitalIdAndAlertLevelAndMetricTypeAndIsActiveTrue(1, "RED", "GLUCOSE")).thenReturn(true);

        // Act & Assert
        try {
            hospitalConfigService.restoreEmergencyGuide(20);
            fail("Nên ném ngoại lệ khi có xung đột bản ghi đang chạy");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Đã có một chỉ dẫn khác đang hoạt động"));
        }
    }

    @Test
    public void testRestoreEmergencyProtocol_ConflictActiveProtocol_ThrowsException() {
        // Arrange
        EmergencyProtocol protocol = new EmergencyProtocol();
        protocol.setId(30);
        protocol.setHospital(hospital);
        protocol.setConditionType("DIABETES");
        protocol.setIsActive(false);

        when(emergencyProtocolRepository.findById(30)).thenReturn(Optional.of(protocol));
        when(emergencyProtocolRepository.existsByHospitalIdAndConditionTypeAndIsActiveTrue(1, "DIABETES")).thenReturn(true);

        // Act & Assert
        try {
            hospitalConfigService.restoreEmergencyProtocol(30);
            fail("Nên ném ngoại lệ khi có xung đột cẩm nang đang chạy");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Đã có một cẩm nang khác đang hoạt động"));
        }
    }

    // =========================================================================
    // TESTS FOR Food Dictionary
    // =========================================================================

    @Test
    public void testAddFood_DuplicateCode_ThrowsException() {
        // Arrange
        FoodDictionary food = new FoodDictionary();
        food.setFoodCode("COM_TAM");
        food.setFoodName("Cơm tấm");

        when(foodDictionaryRepository.existsByFoodCode("COM_TAM")).thenReturn(true);

        // Act & Assert
        try {
            hospitalConfigService.addFood(food);
            fail("Nên ném ngoại lệ khi trùng mã món ăn");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Mã thực phẩm đã tồn tại"));
        }
    }

    @Test
    public void testAddFood_NegativeEnergy_ThrowsException() {
        // Arrange
        FoodDictionary food = new FoodDictionary();
        food.setFoodCode("PHO_BO");
        food.setFoodName("Phở bò");
        food.setEnergyKcal(-5);

        // Act & Assert
        try {
            hospitalConfigService.addFood(food);
            fail("Nên ném ngoại lệ khi calo âm");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Calo (kcal) phải >= 0"));
        }
    }

    @Test
    public void testEditFood_Valid_SavesSuccessfully() {
        // Arrange
        FoodDictionary existing = new FoodDictionary();
        existing.setId(5);
        existing.setFoodCode("PHO_BO");
        existing.setFoodName("Phở bò");

        FoodDictionary updated = new FoodDictionary();
        updated.setFoodCode("PHO_BO_DAC_BIET");
        updated.setFoodName("Phở bò đặc biệt");
        updated.setIsActive(true);

        when(foodDictionaryRepository.findById(5)).thenReturn(Optional.of(existing));
        when(foodDictionaryRepository.existsByFoodCode("PHO_BO_DAC_BIET")).thenReturn(false);
        when(foodDictionaryRepository.save(any(FoodDictionary.class))).thenAnswer(org.mockito.AdditionalAnswers.returnsFirstArg());

        // Act
        FoodDictionary result = hospitalConfigService.editFood(5, updated);

        // Assert
        assertNotNull(result);
        assertEquals("PHO_BO_DAC_BIET", result.getFoodCode());
        assertEquals("Phở bò đặc biệt", result.getFoodName());
        verify(foodDictionaryRepository).save(existing);
    }

    @Test
    public void testToggleFoodActive_Valid_TogglesSuccessfully() {
        // Arrange
        FoodDictionary food = new FoodDictionary();
        food.setId(5);
        food.setIsActive(true);

        when(foodDictionaryRepository.findById(5)).thenReturn(Optional.of(food));
        when(foodDictionaryRepository.save(any(FoodDictionary.class))).thenAnswer(org.mockito.AdditionalAnswers.returnsFirstArg());

        // Act
        FoodDictionary result = hospitalConfigService.toggleFoodActive(5);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsActive());
        verify(foodDictionaryRepository).save(food);
    }
}
