package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.model.ExerciseLog;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.TreatmentPlan;
import com.rpm.remotepatientmonitoring.repository.ExerciseLogRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.TreatmentPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.rpm.remotepatientmonitoring.model.Notification;

@Service
public class ExerciseLogService {

    private static final Logger log = LoggerFactory.getLogger(ExerciseLogService.class);

    public static final int DAILY_GOAL_MINUTES = 30;
    public static final int HIGH_INTENSITY_WARNING_THRESHOLD = 120;

    @Autowired
    private ExerciseLogRepository exerciseLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.NotificationRepository notificationRepository;

    /**
     * Lấy mục tiêu thời lượng tập luyện của bệnh nhân dựa trên phác đồ điều trị hiện tại
     */
    public int getTargetMinutesForPatient(Integer patientId) {
        int targetMinutes = DAILY_GOAL_MINUTES;
        try {
            Optional<TreatmentPlan> planOpt = treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true);
            if (planOpt.isPresent()) {
                TreatmentPlan plan = planOpt.get();
                if (plan.getTargetSteps() != null && plan.getTargetSteps() > 0) {
                    // Quy đổi hợp lý sang phút: 100 bước chân tương đương 1 phút đi bộ
                    targetMinutes = plan.getTargetSteps() / 100;
                    if (targetMinutes <= 0) {
                        targetMinutes = DAILY_GOAL_MINUTES;
                    }
                } else if (plan.getExerciseGoal() != null && !plan.getExerciseGoal().trim().isEmpty()) {
                    // Cố gắng parse số phút từ chuỗi exerciseGoal bằng Regex
                    try {
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*(phút|phut|p|ph|min|minutes)", java.util.regex.Pattern.CASE_INSENSITIVE);
                        java.util.regex.Matcher matcher = pattern.matcher(plan.getExerciseGoal());
                        if (matcher.find()) {
                            int minutes = Integer.parseInt(matcher.group(1));
                            if (minutes > 0) {
                                targetMinutes = minutes;
                            }
                        } else {
                            // Nếu không có từ khóa "phút", lấy số đầu tiên xuất hiện trong chuỗi
                            java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("\\d+");
                            java.util.regex.Matcher numMatcher = numPattern.matcher(plan.getExerciseGoal());
                            if (numMatcher.find()) {
                                int minutes = Integer.parseInt(numMatcher.group());
                                if (minutes > 0 && minutes < 1000) {
                                    targetMinutes = minutes;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Không thể parse exercise_goal '{}' sang số phút, fallback về 30 phút. Lỗi: {}", 
                                plan.getExerciseGoal(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi truy xuất phác đồ điều trị của patientId={}, fallback về 30 phút. Lỗi: {}", 
                    patientId, e.getMessage());
        }
        return targetMinutes;
    }

    /**
     * Lấy tổng kết tập luyện trong ngày hôm nay của bệnh nhân
     */
    public Map<String, Object> getTodaySummary(Integer patientId) {
        LocalDate today = LocalDate.now();
        List<ExerciseLog> logs = exerciseLogRepository.findByPatientIdAndLogDate(patientId, today);

        int totalMinutes = 0;
        double totalCaloriesBurned = 0.0;

        for (ExerciseLog log : logs) {
            if (log.getDurationMinutes() != null) {
                totalMinutes += log.getDurationMinutes();
            }
            if (log.getCaloriesBurned() != null) {
                totalCaloriesBurned += log.getCaloriesBurned();
            }
        }

        int targetMinutes = getTargetMinutesForPatient(patientId);

        int exerciseProgress = 0;
        if (targetMinutes > 0) {
            exerciseProgress = (totalMinutes * 100) / targetMinutes;
            if (exerciseProgress > 100) {
                exerciseProgress = 100;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMinutes", totalMinutes);
        summary.put("totalCaloriesBurned", totalCaloriesBurned);
        summary.put("targetMinutes", targetMinutes);
        summary.put("exerciseProgress", exerciseProgress);

        return summary;
    }

    /**
     * Lấy danh sách tổng hợp tập luyện theo từng ngày trong khoảng thời gian xác định
     */
    public List<Map<String, Object>> getHistorySummary(Integer patientId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<ExerciseLog> logs = exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, endDate);

        // Group by logDate
        Map<LocalDate, Map<String, Object>> dateMap = new HashMap<>();
        for (ExerciseLog elog : logs) {
            LocalDate date = elog.getLogDate();
            Map<String, Object> daySummary = dateMap.computeIfAbsent(date, d -> {
                Map<String, Object> map = new HashMap<>();
                map.put("logDate", d);
                map.put("totalMinutes", 0);
                map.put("totalCaloriesBurned", 0.0);
                return map;
            });
            if (elog.getDurationMinutes() != null) {
                daySummary.put("totalMinutes", (Integer) daySummary.get("totalMinutes") + elog.getDurationMinutes());
            }
            if (elog.getCaloriesBurned() != null) {
                daySummary.put("totalCaloriesBurned", (Double) daySummary.get("totalCaloriesBurned") + elog.getCaloriesBurned());
            }
        }

        // Đảm bảo đầy đủ các ngày trong khoảng thời gian (điền giá trị 0 cho ngày thiếu)
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> daySummary = dateMap.get(date);
            if (daySummary == null) {
                daySummary = new HashMap<>();
                daySummary.put("logDate", date);
                daySummary.put("totalMinutes", 0);
                daySummary.put("totalCaloriesBurned", 0.0);
            }
            daySummary.put("formattedDate", date.format(formatter));
            result.add(daySummary);
        }

        return result;
    }

    /**
     * Lấy danh sách bài tập đã ghi nhận trong ngày hôm nay
     */
    public List<ExerciseLog> getTodayLogs(Integer patientId) {
        return exerciseLogRepository.findByPatientIdAndLogDate(patientId, LocalDate.now());
    }

    /**
     * Lưu một bản ghi bài tập mới
     */
    @Transactional
    public ExerciseLog saveExerciseLog(Integer patientId, String exerciseType, Integer steps,
                                       Integer durationMinutes, Double caloriesBurned) {
        // Validate dữ liệu đầu vào
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Thời lượng tập luyện phải lớn hơn 0 phút!");
        }
        if (steps != null && steps < 0) {
            throw new IllegalArgumentException("Số bước chân không được nhỏ hơn 0!");
        }
        if (exerciseType == null || exerciseType.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn hoặc nhập loại bài tập!");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Bệnh nhân không tồn tại với ID: " + patientId));

        ExerciseLog log = new ExerciseLog();
        log.setPatient(patient);
        log.setLogDate(LocalDate.now());
        log.setExerciseType(exerciseType.trim());
        log.setDurationMinutes(durationMinutes);
        log.setStepsCount(steps);
        log.setCaloriesBurned(caloriesBurned != null ? caloriesBurned : 0.0);
        log.setLoggedAt(LocalDateTime.now());

        return exerciseLogRepository.save(log);
    }

    /**
     * Tính toán chuỗi ngày đạt mục tiêu liên tiếp (Streak) của bệnh nhân
     */
    public int getCurrentStreak(Integer patientId) {
        int targetMinutes = getTargetMinutesForPatient(patientId);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(3650);
        List<ExerciseLog> logs = exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, today);

        // Group by logDate
        Map<LocalDate, Integer> dailyMinutes = new HashMap<>();
        for (ExerciseLog elog : logs) {
            LocalDate date = elog.getLogDate();
            if (elog.getDurationMinutes() != null) {
                dailyMinutes.put(date, dailyMinutes.getOrDefault(date, 0) + elog.getDurationMinutes());
            }
        }

        int streak = 0;
        LocalDate date = today;

        // Nếu hôm nay đã đạt mục tiêu
        if (dailyMinutes.getOrDefault(today, 0) >= targetMinutes) {
            while (dailyMinutes.getOrDefault(date, 0) >= targetMinutes) {
                streak++;
                date = date.minusDays(1);
            }
        } else {
            // Nếu hôm nay chưa đạt, đếm từ ngày hôm qua trở về trước
            date = today.minusDays(1);
            while (dailyMinutes.getOrDefault(date, 0) >= targetMinutes) {
                streak++;
                date = date.minusDays(1);
            }
        }

        return streak;
    }

    /**
     * Cập nhật một bản ghi bài tập đã có
     */
    @Transactional
    public ExerciseLog updateExerciseLog(Integer id, Integer patientId, String exerciseType, Integer steps,
                                         Integer durationMinutes, Double caloriesBurned) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Thời lượng tập luyện phải lớn hơn 0 phút!");
        }
        if (steps != null && steps < 0) {
            throw new IllegalArgumentException("Số bước chân không được nhỏ hơn 0!");
        }
        if (exerciseType == null || exerciseType.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn hoặc nhập loại bài tập!");
        }

        ExerciseLog log = exerciseLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ghi nhận bài tập không tồn tại với ID: " + id));

        // Kiểm tra quyền sở hữu (ownership)
        if (!log.getPatient().getId().equals(patientId)) {
            throw new IllegalStateException("Bạn không có quyền chỉnh sửa ghi nhận này!");
        }

        log.setExerciseType(exerciseType.trim());
        log.setStepsCount(steps);
        log.setDurationMinutes(durationMinutes);
        log.setCaloriesBurned(caloriesBurned != null ? caloriesBurned : 0.0);
        log.setLoggedAt(LocalDateTime.now());

        return exerciseLogRepository.save(log);
    }

    /**
     * Lấy chỉ số BMI từ clinical_records gần nhất của bệnh nhân.
     */
    public Map<String, Object> getLatestBmi(Integer patientId) {
        log.info("Lấy chỉ số BMI cho patientId={}", patientId);
        String sql = "SELECT TOP 1 weight_kg, height_cm, bmi, examination_date " +
                     "FROM clinical_records " +
                     "WHERE patient_id = ? " +
                     "ORDER BY examination_date DESC";
        
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, patientId);
            if (rows.isEmpty()) {
                return null;
            }
            
            Map<String, Object> row = rows.get(0);
            java.math.BigDecimal weight = (java.math.BigDecimal) row.get("weight_kg");
            java.math.BigDecimal height = (java.math.BigDecimal) row.get("height_cm");
            java.math.BigDecimal bmiDb = (java.math.BigDecimal) row.get("bmi");
            Object examDateObj = row.get("examination_date");
            
            if (weight == null || height == null) {
                return null;
            }
            
            double weightVal = weight.doubleValue();
            double heightVal = height.doubleValue();
            double bmiVal;
            
            if (bmiDb != null) {
                bmiVal = bmiDb.doubleValue();
            } else {
                if (heightVal <= 0) {
                    return null;
                }
                bmiVal = weightVal / Math.pow(heightVal / 100.0, 2);
            }
            
            // Round to 1 decimal place
            bmiVal = Math.round(bmiVal * 10.0) / 10.0;
            
            String category;
            if (bmiVal < 18.5) {
                category = "Thiếu cân";
            } else if (bmiVal < 23.0) {
                category = "Bình thường";
            } else if (bmiVal < 25.0) {
                category = "Thừa cân";
            } else {
                category = "Béo phì";
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("weightKg", weightVal);
            result.put("heightCm", heightVal);
            result.put("bmiValue", bmiVal);
            result.put("bmiCategory", category);
            
            if (examDateObj != null) {
                LocalDateTime ldt = null;
                if (examDateObj instanceof java.sql.Timestamp) {
                    ldt = ((java.sql.Timestamp) examDateObj).toLocalDateTime();
                } else if (examDateObj instanceof LocalDateTime) {
                    ldt = (LocalDateTime) examDateObj;
                }
                
                if (ldt != null) {
                    result.put("examinationDate", ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    result.put("examinationDate", examDateObj.toString());
                }
            } else {
                result.put("examinationDate", "N/A");
            }
            
            return result;
        } catch (Exception e) {
            log.error("Lỗi khi lấy chỉ số BMI cho patientId={}: {}", patientId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lấy danh sách các thông báo liên quan tập luyện chưa đọc của ngày hôm nay.
     */
    public List<Notification> getUnreadExerciseNotificationsToday(Integer patientId) {
        log.info("Lấy thông báo tập luyện chưa đọc hôm nay cho patientId={}", patientId);
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        return notificationRepository.findUnreadExerciseNotificationsToday(patientId, start, end);
    }
}
