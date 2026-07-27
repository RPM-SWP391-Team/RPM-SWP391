package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.repository.NotificationRepository;

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
import com.rpm.remotepatientmonitoring.model.ExerciseGuideline;
import com.rpm.remotepatientmonitoring.repository.ExerciseGuidelineRepository;

@Service
public class ExerciseLogService {

    private static final Logger log = LoggerFactory.getLogger(ExerciseLogService.class);

    public static final int DAILY_GOAL_MINUTES = 30;
    public static final int HIGH_CALORIE_WARNING_THRESHOLD = 600;
    public static final int AVERAGE_KCAL_PER_MINUTE = 4;

    @Autowired
    private ExerciseLogRepository exerciseLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ExerciseGuidelineRepository exerciseGuidelineRepository;

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
        if (durationMinutes > 1440) {
            throw new IllegalArgumentException("Thời lượng tập luyện không được vượt quá 1440 phút (24 giờ)!");
        }
        if (steps != null && steps < 0) {
            throw new IllegalArgumentException("Số bước chân không được nhỏ hơn 0!");
        }
        if (steps != null && steps > 100000) {
            throw new IllegalArgumentException("Số bước chân không được vượt quá 100,000 bước!");
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

        ExerciseLog saved = exerciseLogRepository.save(log);
        checkAndReplaceMissedExerciseNotification(patientId);

        return saved;
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
        if (durationMinutes > 1440) {
            throw new IllegalArgumentException("Thời lượng tập luyện không được vượt quá 1440 phút (24 giờ)!");
        }
        if (steps != null && steps < 0) {
            throw new IllegalArgumentException("Số bước chân không được nhỏ hơn 0!");
        }
        if (steps != null && steps > 100000) {
            throw new IllegalArgumentException("Số bước chân không được vượt quá 100,000 bước!");
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

        ExerciseLog saved = exerciseLogRepository.save(log);
        checkAndReplaceMissedExerciseNotification(patientId);

        return saved;
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
     * Lấy danh sách các thông báo liên quan tập luyện chưa đọc.
     */
    public List<Notification> getUnreadExerciseNotificationsToday(Integer patientId) {
        log.info("Lấy thông báo tập luyện chưa đọc cho patientId={}", patientId);
        return notificationRepository.findUnreadExerciseNotifications(patientId);
    }

    /**
     * Tự động cập nhật thông báo "chưa ghi nhận vận động" thành "đã ghi nhận vận động"
     */
    private void checkAndReplaceMissedExerciseNotification(Integer patientId) {
        try {
            Map<String, Object> summary = getTodaySummary(patientId);
            int totalMinutes = summary.get("totalMinutes") != null ? (int) summary.get("totalMinutes") : 0;
            int targetMinutes = summary.get("targetMinutes") != null ? (int) summary.get("targetMinutes") : 30;

            List<Notification> unreadNotifs = notificationRepository.findUnreadExerciseNotifications(patientId);
            for (Notification n : unreadNotifs) {
                if ("EXERCISE_REMINDER".equals(n.getNotificationType()) 
                        && n.getContent() != null 
                        && n.getContent().contains("chưa ghi nhận vận động")) {
                    if (totalMinutes >= targetMinutes) {
                        n.setTitle("Đạt mục tiêu tập luyện");
                        n.setContent("Chúc mừng! Bạn đã đạt mục tiêu vận động hôm nay 🎉");
                    } else {
                        n.setTitle("Đã ghi nhận tập luyện");
                        n.setContent("Bạn đã ghi nhận vận động hôm nay. Tiếp tục cố gắng để đạt mục tiêu nhé!");
                    }
                    notificationRepository.save(n);
                } else if ("EXERCISE_STREAK_AT_RISK".equals(n.getNotificationType())) {
                    if (totalMinutes >= targetMinutes) {
                        n.setTitle("Đạt mục tiêu tập luyện");
                        n.setContent("Chúc mừng! Bạn đã đạt mục tiêu vận động hôm nay 🎉");
                        notificationRepository.save(n);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi thông báo chưa tập luyện cho patient={}: {}", patientId, e.getMessage());
        }
    }

    @Transactional
    public void deleteExerciseLog(Integer id, Integer patientId) {
        ExerciseLog log = exerciseLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ghi nhận bài tập."));
        if (!log.getPatient().getId().equals(patientId)) {
            throw new IllegalStateException("Bạn không có quyền xóa ghi nhận này!");
        }
        exerciseLogRepository.delete(log);
        checkAndReplaceMissedExerciseNotification(patientId);
    }

    /**
     * Kiểm tra xem bệnh nhân có nguy cơ mất streak tập luyện hôm nay hay không.
     * Trả về true nếu streak hiện tại > 0 VÀ tổng phút vận động hôm nay < mục tiêu hôm nay.
     */
    public boolean isStreakAtRiskToday(Integer patientId) {
        int streak = getCurrentStreak(patientId);
        if (streak <= 0) {
            return false;
        }

        Map<String, Object> summary = getTodaySummary(patientId);
        int totalMinutes = summary.get("totalMinutes") != null ? (int) summary.get("totalMinutes") : 0;
        int targetMinutes = summary.get("targetMinutes") != null ? (int) summary.get("targetMinutes") : 30;

        return totalMinutes < targetMinutes;
    }

    public static class WeeklyCompliance {
        private final int daysAchieved;
        private final int totalDays;

        public WeeklyCompliance(int daysAchieved, int totalDays) {
            this.daysAchieved = daysAchieved;
            this.totalDays = totalDays;
        }

        public int getDaysAchieved() {
            return daysAchieved;
        }

        public int getTotalDays() {
            return totalDays;
        }
    }

    /**
     * Đo lường mức độ tuân thủ theo khuyến nghị trong 7 ngày gần nhất.
     */
    public WeeklyCompliance getWeeklyComplianceRate(Integer patientId) {
        int targetMinutes = getTargetMinutesForPatient(patientId);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        List<ExerciseLog> logs = exerciseLogRepository.findByPatientIdAndLogDateBetween(patientId, startDate, today);

        // Group by logDate
        Map<LocalDate, Integer> dailyMinutes = new HashMap<>();
        for (ExerciseLog elog : logs) {
            LocalDate date = elog.getLogDate();
            if (elog.getDurationMinutes() != null) {
                dailyMinutes.put(date, dailyMinutes.getOrDefault(date, 0) + elog.getDurationMinutes());
            }
        }

        int daysAchieved = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            int minutes = dailyMinutes.getOrDefault(date, 0);
            if (minutes >= targetMinutes) {
                daysAchieved++;
            }
        }

        return new WeeklyCompliance(daysAchieved, 7);
    }

    /**
     * Lấy văn bản khuyến nghị vận động dựa trên treatment plan hoặc disease profile.
     */
    public String getExerciseRecommendation(Integer patientId) {
        // 1. Kiểm tra phác đồ điều trị hiện tại
        try {
            Optional<TreatmentPlan> planOpt = treatmentPlanRepository.findByPatientIdAndIsCurrent(patientId, true);
            if (planOpt.isPresent()) {
                TreatmentPlan plan = planOpt.get();
                if (plan.getExerciseGoal() != null && !plan.getExerciseGoal().trim().isEmpty()) {
                    return plan.getExerciseGoal().trim();
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy phác đồ điều trị cho patientId={}: {}", patientId, e.getMessage());
        }

        // 2. Dự phòng theo disease profile
        try {
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                if (patient.getDiseaseProfile() != null) {
                    String code = patient.getDiseaseProfile().getProfileCode();
                    if ("HYPERTENSION".equalsIgnoreCase(code)) {
                        return "Nên đi bộ nhanh, đạp xe nhẹ hoặc tập yoga 30 phút/ngày, 5 ngày/tuần. Tránh vận động gắng sức đột ngột, nên khởi động kỹ trước khi tập.";
                    } else if ("DIABETES".equalsIgnoreCase(code)) {
                        return "Nên vận động đều đặn 30 phút/ngày giúp cải thiện độ nhạy insulin. Ưu tiên đi bộ sau bữa ăn, tránh tập lúc đói hoặc đường huyết đang thấp.";
                    } else if ("BOTH".equalsIgnoreCase(code)) {
                        return "Nên đi bộ nhanh, đạp xe nhẹ hoặc tập yoga 30 phút/ngày, 5 ngày/tuần. Tránh vận động gắng sức đột ngột, nên khởi động kỹ trước khi tập.\nNên vận động đều đặn 30 phút/ngày giúp cải thiện độ nhạy insulin. Ưu tiên đi bộ sau bữa ăn, tránh tập lúc đói hoặc đường huyết đang thấp.";
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin bệnh nhân/disease profile cho patientId={}: {}", patientId, e.getMessage());
        }

        // 3. Dự phòng chung
        return "Thực hiện vận động thể chất ít nhất 30 phút mỗi ngày giúp tăng cường sức khỏe tim mạch, cải thiện độ nhạy insulin và kiểm soát đường huyết hiệu quả. Hãy lựa chọn các bài tập vừa sức như đi bộ nhanh, đạp xe nhẹ nhàng hoặc tập yoga.";
    }

    /**
     * Lấy hướng dẫn vận động phù hợp với disease_profile_id và hospital_id của bệnh nhân.
     */
    public Optional<ExerciseGuideline> getExerciseGuideline(Integer patientId) {
        try {
            Optional<Patient> patientOpt = patientRepository.findById(patientId);
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                if (patient.getDiseaseProfile() != null && patient.getHospital() != null) {
                    return exerciseGuidelineRepository.findByDiseaseProfileIdAndHospitalIdAndIsActiveTrue(
                            patient.getDiseaseProfile().getId(),
                            patient.getHospital().getId()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi truy xuất exercise guideline cho patientId={}: {}", patientId, e.getMessage());
        }
        return Optional.empty();
    }
}
