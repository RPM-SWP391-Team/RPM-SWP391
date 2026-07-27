package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private NutritionRuleRepository nutritionRuleRepository;

    @Autowired
    private PatientMedicationRepository patientMedicationRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private WaterLogRepository waterLogRepository;

    @Autowired
    private PatientMealRepository patientMealRepository;

    @Autowired
    private PatientExerciseRepository patientExerciseRepository;

    @Autowired
    private FoodDictionaryRepository foodDictionaryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Patient> findByAccountId(Integer accountId) {
        return patientRepository.findByAccountId(accountId);
    }

    public List<Patient> findAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<TreatmentPlan> findCurrentTreatmentPlan(Integer patientId) {
        return treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true);
    }

    public Optional<NutritionRule> findCurrentNutritionRule(Integer patientId) {
        return nutritionRuleRepository.findByPatientIdAndIsCurrent(patientId, true);
    }

    public List<PatientMedication> findActiveMedications(Integer patientId) {
        return patientMedicationRepository.findByPatientIdAndIsActiveTrue(patientId);
    }

    public Optional<MedicationLog> findMedicationLog(Integer medId, LocalDate date) {
        return medicationLogRepository.findByPatientMedicationIdAndLogDate(medId, date);
    }

    public Optional<WaterLog> findWaterLog(Integer patientId, LocalDate date) {
        return waterLogRepository.findByPatientIdAndLogDate(patientId, date);
    }

    public List<PatientMeal> findMealsByDate(Integer patientId, LocalDate date) {
        return patientMealRepository.findByPatientIdAndLogDate(patientId, date);
    }

    public List<PatientExercise> findExercisesByDate(Integer patientId, LocalDate date) {
        return patientExerciseRepository.findByPatientIdAndLogDate(patientId, date);
    }

    public List<MedicationLog> findMedicationHistory(Integer patientId, LocalDate startDate) {
        if (startDate != null) {
            return medicationLogRepository.findByPatientMedicationPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patientId, startDate);
        } else {
            return medicationLogRepository.findByPatientMedicationPatientIdOrderByLogDateAsc(patientId);
        }
    }

    public List<WaterLog> findWaterHistory(Integer patientId, LocalDate startDate) {
        if (startDate != null) {
            return waterLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patientId, startDate);
        } else {
            return waterLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patientId, LocalDate.of(2000, 1, 1));
        }
    }

    public List<FoodDictionary> findActiveFoods() {
        return foodDictionaryRepository.findByIsActiveTrue();
    }

    @Transactional
    public void savePatient(Patient patient) {
        patientRepository.save(patient);
    }

    @Transactional
    public void saveAccount(Account account) {
        accountRepository.save(account);
    }

    @Transactional
    public int addWater(Patient patient, Integer amount) {
        LocalDate today = LocalDate.now();
        List<WaterLog> existingLogs = waterLogRepository.findAllByPatientIdAndLogDate(patient.getId(), today);
        if (!existingLogs.isEmpty()) {
            WaterLog firstLog = existingLogs.get(0);
            firstLog.setAmountMl(firstLog.getAmountMl() + amount);
            firstLog.setLoggedAt(java.time.LocalDateTime.now());
            waterLogRepository.save(firstLog);
        } else {
            WaterLog log = WaterLog.builder()
                    .patient(patient)
                    .logDate(today)
                    .amountMl(amount)
                    .loggedAt(java.time.LocalDateTime.now())
                    .build();
            waterLogRepository.save(log);
        }
        
        List<WaterLog> logs = waterLogRepository.findAllByPatientIdAndLogDate(patient.getId(), today);
        int totalAmount = 0;
        for (WaterLog log : logs) {
            if (log != null && log.getAmountMl() != null) {
                totalAmount += log.getAmountMl();
            }
        }
        return totalAmount;
    }

    @Transactional
    public int resetWater(Patient patient) {
        LocalDate today = LocalDate.now();
        List<WaterLog> logs = waterLogRepository.findAllByPatientIdAndLogDate(patient.getId(), today);
        waterLogRepository.deleteAll(logs);
        return 0;
    }

    @Transactional
    public PatientMeal addMeal(Patient patient, String mealType, Integer foodId, Double quantityG) {
        Optional<FoodDictionary> foodOpt = foodDictionaryRepository.findById(foodId);
        if (foodOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy món ăn trong từ điển.");
        }
        FoodDictionary food = foodOpt.get();

        PatientMeal meal = new PatientMeal();
        meal.setPatient(patient);
        meal.setLogDate(LocalDate.now());
        meal.setMealType(mealType);
        meal.setFood(food);
        meal.setQuantityG(quantityG);

        return patientMealRepository.save(meal);
    }

    @Transactional
    public void deleteMeal(Integer id) {
        patientMealRepository.deleteById(id);
    }

    @Transactional
    public PatientMeal updateMeal(Integer id, Double quantityG) {
        Optional<PatientMeal> opt = patientMealRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy bản ghi bữa ăn.");
        }
        PatientMeal meal = opt.get();
        meal.setQuantityG(quantityG);
        return patientMealRepository.save(meal);
    }

    @Transactional
    public PatientExercise addPatientExercise(Patient patient, String exerciseType, Integer durationMinutes, Integer stepsCount) {
        PatientExercise exercise = new PatientExercise();
        exercise.setPatient(patient);
        exercise.setLogDate(LocalDate.now());
        exercise.setExerciseType(exerciseType);
        exercise.setDurationMinutes(durationMinutes);
        exercise.setStepsCount(stepsCount);
        return patientExerciseRepository.save(exercise);
    }

    @Transactional
    public void deletePatientExercise(Integer id) {
        patientExerciseRepository.deleteById(id);
    }

    @Transactional
    public void updateProfile(Patient patient, String phone, String address, String emergencyContactName, String emergencyContactPhone, String password) {
        patient.setPhone(phone.trim());
        patient.setAddress(address != null ? address.trim() : "");
        patient.setEmergencyContactName(emergencyContactName != null && !emergencyContactName.trim().isEmpty() ? emergencyContactName.trim() : null);
        patient.setEmergencyContactPhone(emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty() ? emergencyContactPhone.trim() : null);
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        Account account = patient.getAccount();
        if (account != null) {
            if (password != null && !password.trim().isEmpty()) {
                account.setPasswordHash(passwordEncoder.encode(password.trim()));
            }
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
        }
    }

    @Transactional
    public WaterLog addOrUpdateWaterLog(Integer id, Patient patient, LocalDate date, int amount) {
        if (id != null) {
            Optional<WaterLog> logOpt = waterLogRepository.findById(id);
            if (logOpt.isPresent()) {
                WaterLog log = logOpt.get();
                log.setAmountMl(amount);
                log.setLoggedAt(java.time.LocalDateTime.now());
                return waterLogRepository.save(log);
            }
        }
        List<WaterLog> existingLogs = waterLogRepository.findAllByPatientIdAndLogDate(patient.getId(), date);
        if (!existingLogs.isEmpty()) {
            WaterLog log = existingLogs.get(0);
            log.setAmountMl(amount);
            log.setLoggedAt(java.time.LocalDateTime.now());
            return waterLogRepository.save(log);
        }
        WaterLog log = WaterLog.builder()
                .patient(patient)
                .logDate(date)
                .amountMl(amount)
                .loggedAt(java.time.LocalDateTime.now())
                .build();
        return waterLogRepository.save(log);
    }

    @Transactional
    public void deleteWaterLog(Integer id) {
        Optional<WaterLog> logOpt = waterLogRepository.findById(id);
        if (logOpt.isPresent()) {
            waterLogRepository.delete(logOpt.get());
        }
    }

    public List<WaterLog> findWaterLogs(Integer patientId, LocalDate date) {
        return waterLogRepository.findAllByPatientIdAndLogDate(patientId, date);
    }
}

