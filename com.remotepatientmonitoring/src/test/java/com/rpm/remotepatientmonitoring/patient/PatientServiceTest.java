package com.rpm.remotepatientmonitoring.patient;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import com.rpm.remotepatientmonitoring.service.patient.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WaterLogRepository waterLogRepository;

    @Mock
    private PatientMealRepository patientMealRepository;

    @Mock
    private FoodDictionaryRepository foodDictionaryRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1);
        patient.setFullName("Test Patient");
    }

    @Test
    void addWater_UT01() {
        Integer amount = 250;
        WaterLog savedLog = new WaterLog();
        savedLog.setAmountMl(250);

        List<WaterLog> emptyList = new ArrayList<>();
        List<WaterLog> logList = new ArrayList<>();
        logList.add(savedLog);

        when(waterLogRepository.findAllByPatientIdAndLogDate(eq(1), any()))
                .thenReturn(emptyList)
                .thenReturn(logList);

        int actual = patientService.addWater(patient, amount);
        assertEquals(250, actual);
        verify(waterLogRepository, times(1)).save(any(WaterLog.class));
    }

    @Test
    void addWater_UT02() {
        Integer amount = 250;
        WaterLog existing = new WaterLog();
        existing.setAmountMl(500);

        List<WaterLog> existingLogs = new ArrayList<>();
        existingLogs.add(existing);
        when(waterLogRepository.findAllByPatientIdAndLogDate(eq(1), any())).thenReturn(existingLogs);

        int actual = patientService.addWater(patient, amount);
        assertEquals(750, existing.getAmountMl());
        assertEquals(750, actual);
    }

    @Test
    void resetWater_UT03() {
        WaterLog existing = new WaterLog();
        List<WaterLog> existingLogs = new ArrayList<>();
        existingLogs.add(existing);
        when(waterLogRepository.findAllByPatientIdAndLogDate(eq(1), any())).thenReturn(existingLogs);

        int actual = patientService.resetWater(patient);
        assertEquals(0, actual);
        verify(waterLogRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void addMeal_UT04() {
        String mealType = "BREAKFAST";
        Integer foodId = 5;
        Double quantityG = 150.0;

        FoodDictionary food = new FoodDictionary();
        food.setId(5);
        food.setEnergyKcal(200);

        PatientMeal createdMeal = new PatientMeal();
        createdMeal.setMealType("BREAKFAST");
        createdMeal.setQuantityG(150.0);

        when(foodDictionaryRepository.findById(5)).thenReturn(Optional.of(food));
        when(patientMealRepository.save(any(PatientMeal.class))).thenReturn(createdMeal);

        PatientMeal actual = patientService.addMeal(patient, mealType, foodId, quantityG);
        assertNotNull(actual);
        assertEquals("BREAKFAST", actual.getMealType());
        assertEquals(150.0, actual.getQuantityG());
    }

    @Test
    void addMeal_UT05() {
        String mealType = "BREAKFAST";
        Integer foodId = 999;
        Double quantityG = 100.0;

        when(foodDictionaryRepository.findById(999)).thenReturn(Optional.empty());

        try {
            patientService.addMeal(patient, mealType, foodId, quantityG);
            fail("Nên quăng IllegalArgumentException khi foodId không tồn tại");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void deleteMeal_UT06() {
        Integer mealId = 10;
        patientService.deleteMeal(mealId);
        verify(patientMealRepository, times(1)).deleteById(mealId);
    }
}
