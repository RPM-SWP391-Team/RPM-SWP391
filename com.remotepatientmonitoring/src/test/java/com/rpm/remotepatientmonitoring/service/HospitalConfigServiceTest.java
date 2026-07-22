package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.service.hospital.HospitalConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpm.remotepatientmonitoring.model.DiseaseProfile;
import com.rpm.remotepatientmonitoring.model.ExerciseGuideline;
import com.rpm.remotepatientmonitoring.model.FoodDictionary;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.repository.DiseaseProfileRepository;
import com.rpm.remotepatientmonitoring.repository.ExerciseGuidelineRepository;
import com.rpm.remotepatientmonitoring.repository.FoodDictionaryRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import com.rpm.remotepatientmonitoring.repository.PatientMealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HospitalConfigServiceTest {

    @Mock
    private ExerciseGuidelineRepository exerciseGuidelineRepository;

    @Mock
    private DiseaseProfileRepository diseaseProfileRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private FoodDictionaryRepository foodDictionaryRepository;

    @Mock
    private PatientMealRepository patientMealRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private HospitalConfigService hospitalConfigService;

    private Hospital hospital;
    private DiseaseProfile diseaseProfile;

    @BeforeEach
    void setUp() {
        hospital = new Hospital();
        hospital.setId(1);
        
        diseaseProfile = new DiseaseProfile();
        diseaseProfile.setId(1);
        diseaseProfile.setProfileCode("DIABETES");
        diseaseProfile.setProfileName("Tiểu đường");
    }

    @Test
    void testAddExerciseGuideline_DuplicateDiseaseProfile_ThrowsException() {
        // Arrange
        Integer hospitalId = 1;
        Integer diseaseProfileId = 1;
        
        ExerciseGuideline existingGuideline = new ExerciseGuideline();
        existingGuideline.setId(10);
        existingGuideline.setIsActive(true);

        when(exerciseGuidelineRepository.findByDiseaseProfileIdAndHospitalIdAndIsActiveTrue(diseaseProfileId, hospitalId))
                .thenReturn(Optional.of(existingGuideline));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hospitalConfigService.addExerciseGuideline(hospitalId, diseaseProfileId, "Title", "Recommended content must be long enough", "Avoid content must be long enough");
        });

        assertTrue(exception.getMessage().contains("diseaseProfileId:Nhóm bệnh này đã có khuyến nghị đang áp dụng. Vui lòng sửa bản ghi hiện có thay vì tạo mới."));
        verify(exerciseGuidelineRepository, never()).save(any(ExerciseGuideline.class));
    }

    @Test
    void testEditExerciseGuideline_DuplicateDiseaseProfile_ThrowsException() {
        // Arrange
        Integer guidelineId = 100;
        Integer newDiseaseProfileId = 2;
        
        ExerciseGuideline existingGuideline = new ExerciseGuideline();
        existingGuideline.setId(guidelineId);
        existingGuideline.setHospital(hospital);
        
        DiseaseProfile oldProfile = new DiseaseProfile();
        oldProfile.setId(1);
        existingGuideline.setDiseaseProfile(oldProfile);

        ExerciseGuideline otherActiveGuideline = new ExerciseGuideline();
        otherActiveGuideline.setId(200); // Another active guideline of profile 2
        otherActiveGuideline.setIsActive(true);

        when(exerciseGuidelineRepository.findById(guidelineId)).thenReturn(Optional.of(existingGuideline));
        when(exerciseGuidelineRepository.findByDiseaseProfileIdAndHospitalIdAndIsActiveTrue(newDiseaseProfileId, hospital.getId()))
                .thenReturn(Optional.of(otherActiveGuideline));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hospitalConfigService.editExerciseGuideline(guidelineId, newDiseaseProfileId, "New Title", "Recommended content must be long enough", "Avoid content must be long enough");
        });

        assertTrue(exception.getMessage().contains("diseaseProfileId:Nhóm bệnh này đã có khuyến nghị đang áp dụng. Vui lòng sửa bản ghi hiện có thay vì tạo mới."));
        verify(exerciseGuidelineRepository, never()).save(any(ExerciseGuideline.class));
    }

    @Test
    void testAddFood_DuplicateFoodCode_ThrowsException() {
        // Arrange
        FoodDictionary food = new FoodDictionary();
        food.setFoodCode("COM_TAM");
        food.setFoodName("Cơm tấm");
        
        when(foodDictionaryRepository.existsByFoodCode("COM_TAM")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hospitalConfigService.addFood(food);
        });

        assertTrue(exception.getMessage().contains("foodCode:Mã thực phẩm đã tồn tại."));
        verify(foodDictionaryRepository, never()).save(any(FoodDictionary.class));
    }

    @Test
    void testEditFood_DuplicateFoodCode_ThrowsException() {
        // Arrange
        Integer foodId = 1;
        FoodDictionary existing = new FoodDictionary();
        existing.setId(foodId);
        existing.setFoodCode("COM_TAM");
        existing.setFoodName("Cơm tấm");

        FoodDictionary updated = new FoodDictionary();
        updated.setFoodCode("PHO_BO");
        updated.setFoodName("Phở bò");

        when(foodDictionaryRepository.findById(foodId)).thenReturn(Optional.of(existing));
        when(foodDictionaryRepository.existsByFoodCode("PHO_BO")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hospitalConfigService.editFood(foodId, updated);
        });

        assertTrue(exception.getMessage().contains("foodCode:Mã thực phẩm đã tồn tại."));
        verify(foodDictionaryRepository, never()).save(any(FoodDictionary.class));
    }

    @Test
    void testAddFood_NegativeNutrients_ThrowsException() {
        // Arrange
        FoodDictionary food = new FoodDictionary();
        food.setFoodCode("COM_TAM");
        food.setFoodName("Cơm tấm");
        food.setEnergyKcal(-10); // Negative nutrient

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hospitalConfigService.addFood(food);
        });

        assertTrue(exception.getMessage().contains("energyKcal:Calo (kcal) phải >= 0"));
        verify(foodDictionaryRepository, never()).save(any(FoodDictionary.class));
    }

    @Test
    void testEditFood_NegativeNutrients_ThrowsException() {
        // Arrange
        Integer foodId = 1;
        FoodDictionary existing = new FoodDictionary();
        existing.setId(foodId);
        existing.setFoodCode("COM_TAM");
        existing.setFoodName("Cơm tấm");

        FoodDictionary updated = new FoodDictionary();
        updated.setFoodCode("COM_TAM");
        updated.setFoodName("Cơm tấm");
        updated.setWaterG(new BigDecimal("-1.50")); // Negative decimal

        when(foodDictionaryRepository.findById(foodId)).thenReturn(Optional.of(existing));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            hospitalConfigService.editFood(foodId, updated);
        });

        assertTrue(exception.getMessage().contains("waterG:Nước (g) phải >= 0"));
        verify(foodDictionaryRepository, never()).save(any(FoodDictionary.class));
    }
}
