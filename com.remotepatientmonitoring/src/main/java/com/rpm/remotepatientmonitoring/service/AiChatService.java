package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.dto.AiChatRequest;
import com.rpm.remotepatientmonitoring.dto.AiChatResponse;
import com.rpm.remotepatientmonitoring.dto.AiSearchRequest;
import com.rpm.remotepatientmonitoring.dto.AiSearchResponse;
import com.rpm.remotepatientmonitoring.dto.PatientAdaStats;
import com.rpm.remotepatientmonitoring.dto.PatientBpStats;
import com.rpm.remotepatientmonitoring.dto.AiSummaryRequest;
import com.rpm.remotepatientmonitoring.dto.AiSummaryResponse;
import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import com.rpm.remotepatientmonitoring.model.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.TreatmentPlan;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.TreatmentPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class AiChatService {

    private final RestTemplate restTemplate;
    private final String AI_API_URL = "http://localhost:8000/api/chat";
    private final String AI_API_SEARCH_URL = "http://localhost:8000/api/search";

    public AiChatService() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(45000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.DoctorRepository doctorRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.HealthLogRepository healthLogRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.AuditTrailRepository auditTrailRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.AiChatHistoryRepository aiChatHistoryRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.AiClinicalSummaryRepository aiClinicalSummaryRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.AlertRepository alertRepository;

    public String buildContext(String email, Integer patientId) {
        try {
            Optional<Account> accountOpt = accountRepository.findByEmail(email);
            if (accountOpt.isEmpty()) return "Người dùng ẩn danh";
            
            Account account = accountOpt.get();
            
            if ("ROLE_DOCTOR".equals(account.getRole())) {
                Optional<com.rpm.remotepatientmonitoring.model.Doctor> docOpt = doctorRepository.findByAccountId(account.getId());
                if (docOpt.isPresent()) {
                    com.rpm.remotepatientmonitoring.model.Doctor doc = docOpt.get();
                    StringBuilder docContext = new StringBuilder();
                    docContext.append(String.format("Bác sĩ %s, chuyên khoa %s tại %s. Người dùng đang hỏi với tư cách là Bác sĩ cần tư vấn chuyên môn y khoa. ", 
                        doc.getFullName(), doc.getSpecialty(), doc.getHospital().getFullName()));
                        
                    // Nếu bác sĩ đang xem hồ sơ của một bệnh nhân cụ thể
                    if (patientId != null) {
                        Optional<Patient> patientOpt = patientRepository.findById(patientId);
                        if (patientOpt.isPresent()) {
                            docContext.append("Đang xem hồ sơ bệnh nhân: ").append(buildPatientContextString(patientOpt.get()));
                        }
                    }
                    return docContext.toString();
                }
                return "Bác sĩ chưa có thông tin chi tiết.";
            } else if ("ROLE_PATIENT".equals(account.getRole())) {
                Optional<Patient> patientOpt = patientRepository.findByAccountId(account.getId());
                if (patientOpt.isEmpty()) return "Bệnh nhân ẩn danh";
                
                return buildPatientContextString(patientOpt.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Không thể tải thông tin.";
    }
    
    private String buildPatientContextString(Patient patient) {
        StringBuilder context = new StringBuilder();
        
        // Thông tin cơ bản
        int age = patient.getDateOfBirth() != null ? Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears() : 0;
        String genderStr = "Nam".equalsIgnoreCase(patient.getGender()) ? "Nam" : ("Nữ".equalsIgnoreCase(patient.getGender()) ? "Nữ" : "Không xác định");
        context.append(String.format("Bệnh nhân %s, %d tuổi. ", genderStr, age));
        
        // Bệnh lý nền
        if (patient.getDiseaseProfile() != null) {
            context.append("Bệnh lý nền: ").append(patient.getDiseaseProfile().getProfileName()).append(". ");
        }
        
        // Phác đồ điều trị hiện tại
        Optional<TreatmentPlan> planOpt = treatmentPlanRepository.findByPatientIdAndIsCurrent(patient.getId(), true);
        if (planOpt.isPresent()) {
            TreatmentPlan plan = planOpt.get();
            if (plan.getMedicalOrder() != null && !plan.getMedicalOrder().isEmpty()) {
                context.append("Đang sử dụng thuốc: ").append(plan.getMedicalOrder().replace("\n", " ")).append(". ");
            }
            if (plan.getExerciseGoal() != null && !plan.getExerciseGoal().isEmpty()) {
                context.append("Chỉ định tập luyện: ").append(plan.getExerciseGoal().replace("\n", " ")).append(". ");
            }
        }
        return context.toString();
    }

    public AiChatResponse getChatbotResponse(String email, String question, String userContext) {
        // AI chỉ dành cho bác sĩ
        try {
            Optional<Account> accountOpt = accountRepository.findByEmail(email);
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                if ("ROLE_PATIENT".equals(account.getRole())) {
                    AiChatResponse errorResponse = new AiChatResponse();
                    errorResponse.setAnswer("Xin lỗi, hệ thống AI RAG hiện tại chỉ dành cho bác sĩ (Clinical Decision Support). Bệnh nhân vui lòng tham khảo ý kiến bác sĩ phụ trách.");
                    return errorResponse;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        AiChatRequest request = new AiChatRequest(email, null, question, userContext);
        
        try {
            ResponseEntity<AiChatResponse> response = restTemplate.postForEntity(
                AI_API_URL, 
                request, 
                AiChatResponse.class
            );
            
            AiChatResponse body = response.getBody();
            if (body != null) {
                body.setDisclaimer("Thông tin chỉ mang tính chất tham khảo cho Bác sĩ (CDSS). Quyết định điều trị cuối cùng thuộc về Bác sĩ chuyên khoa.");
                body.setConfidenceLevel("HIGH");
                if (body.getAnswer() != null && !body.getAnswer().isEmpty()) {
                    String[] lines = body.getAnswer().split("\n");
                    body.setSummaryTakeaway(lines[0].replace("#", "").trim());
                }

                if (body.getAnswer() != null) {
                    try {
                        Optional<Account> accountOpt = accountRepository.findByEmail(email);
                        Integer accId = accountOpt.map(Account::getId).orElse(0);
                        
                        String citationsJson = null;
                        if (body.getCitations() != null && !body.getCitations().isEmpty()) {
                            citationsJson = body.getCitations().toString();
                        }
                        
                        com.rpm.remotepatientmonitoring.model.AiChatHistory chatHistory = 
                            new com.rpm.remotepatientmonitoring.model.AiChatHistory(
                                accId, 
                                null, 
                                question, 
                                body.getAnswer(), 
                                citationsJson
                            );
                        aiChatHistoryRepository.save(chatHistory);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
            return body;
        } catch (Exception e) {

            e.printStackTrace();
            AiChatResponse errorResponse = new AiChatResponse();
            errorResponse.setAnswer("Xin lỗi, hệ thống AI hiện tại đang gặp sự cố. Vui lòng thử lại sau. (Lỗi: " + e.getMessage() + ")");
            return errorResponse;
        }
    }

    public AiSearchResponse getSemanticSearchResults(String query) {
        AiSearchRequest request = new AiSearchRequest(query);
        try {
            ResponseEntity<AiSearchResponse> response = restTemplate.postForEntity(
                AI_API_SEARCH_URL, 
                request, 
                AiSearchResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            AiSearchResponse errorResponse = new AiSearchResponse();
            errorResponse.setTotal_found(0);
            return errorResponse;
        }
    }

    public PatientAdaStats calculateAdaStats(Integer patientId) {
        LocalDate startDate = LocalDate.now().minusDays(14);
        List<DailyHealthLog> logs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patientId, startDate);
        
        List<DailyHealthLog> glucoseLogs = logs.stream()
                .filter(log -> log.getGlucoseLevel() != null)
                .collect(java.util.stream.Collectors.toList());
                
        if (glucoseLogs.size() < 10) {
            return null; // Not enough data
        }
        
        double targetMin = 3.9;
        double targetMax = 10.0;
        
        int tirCount = 0;
        int tarCount = 0;
        int tbrCount = 0;
        double sum = 0.0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (DailyHealthLog log : glucoseLogs) {
            double val = log.getGlucoseLevel().doubleValue();
            sum += val;
            if (val < min) min = val;
            if (val > max) max = val;
            
            if (val < targetMin) tbrCount++;
            else if (val > targetMax) tarCount++;
            else tirCount++;
        }
        
        int total = glucoseLogs.size();
        double mean = sum / total;
        
        double varianceSum = 0.0;
        for (DailyHealthLog log : glucoseLogs) {
            double val = log.getGlucoseLevel().doubleValue();
            varianceSum += Math.pow(val - mean, 2);
        }
        double stdDev = Math.sqrt(varianceSum / total);
        double cv = (stdDev / mean) * 100;
        
        double meanMgDl = mean * 18.0;
        double gmi = 3.31 + (0.02392 * meanMgDl);
        
        PatientAdaStats stats = new PatientAdaStats();
        stats.setTotalReadings(total);
        stats.setMeanGlucose(Math.round(mean * 100.0) / 100.0);
        stats.setMinGlucose(min);
        stats.setMaxGlucose(max);
        stats.setTir((double) tirCount / total * 100);
        stats.setTar((double) tarCount / total * 100);
        stats.setTbr((double) tbrCount / total * 100);
        stats.setCv(Math.round(cv * 100.0) / 100.0);
        stats.setGmi(Math.round(gmi * 100.0) / 100.0);
        
        return stats;
    }

    public PatientBpStats calculateBpStats(Integer patientId) {
        LocalDate startDate = LocalDate.now().minusDays(14);
        List<DailyHealthLog> logs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patientId, startDate);
        
        List<DailyHealthLog> bpLogs = logs.stream()
                .filter(log -> log.getSystolicBp() != null && log.getDiastolicBp() != null)
                .collect(java.util.stream.Collectors.toList());
                
        if (bpLogs.size() < 10) {
            return null; // Not enough data
        }
        
        double sumSbp = 0;
        double sumDbp = 0;
        double morningSumSbp = 0;
        double morningSumDbp = 0;
        int morningCount = 0;
        double eveningSumSbp = 0;
        double eveningSumDbp = 0;
        int eveningCount = 0;
        int highestSbp = 0;
        int highestDbp = 0;
        int lowestSbp = Integer.MAX_VALUE;
        int lowestDbp = Integer.MAX_VALUE;
        int highBpCount = 0;
        int veryHighBpCount = 0;
        
        for (DailyHealthLog log : bpLogs) {
            int sbp = log.getSystolicBp();
            int dbp = log.getDiastolicBp();
            
            sumSbp += sbp;
            sumDbp += dbp;
            
            if (sbp > highestSbp) highestSbp = sbp;
            if (dbp > highestDbp) highestDbp = dbp;
            if (sbp < lowestSbp) lowestSbp = sbp;
            if (dbp < lowestDbp) lowestDbp = dbp;
            
            if (sbp >= 180 || dbp >= 120) {
                veryHighBpCount++;
            } else if (sbp >= 140 || dbp >= 90) {
                highBpCount++;
            }
            
            int hour = log.getLogTime().getHour();
            if (hour >= 5 && hour < 12) {
                morningSumSbp += sbp;
                morningSumDbp += dbp;
                morningCount++;
            } else if (hour >= 17 && hour < 24) {
                eveningSumSbp += sbp;
                eveningSumDbp += dbp;
                eveningCount++;
            }
        }
        
        int total = bpLogs.size();
        PatientBpStats stats = new PatientBpStats();
        stats.setTotalReadings(total);
        stats.setAverageSbp(Math.round(sumSbp / total * 10.0) / 10.0);
        stats.setAverageDbp(Math.round(sumDbp / total * 10.0) / 10.0);
        stats.setMorningAvgSbp(morningCount > 0 ? Math.round(morningSumSbp / morningCount * 10.0) / 10.0 : 0.0);
        stats.setMorningAvgDbp(morningCount > 0 ? Math.round(morningSumDbp / morningCount * 10.0) / 10.0 : 0.0);
        stats.setEveningAvgSbp(eveningCount > 0 ? Math.round(eveningSumSbp / eveningCount * 10.0) / 10.0 : 0.0);
        stats.setEveningAvgDbp(eveningCount > 0 ? Math.round(eveningSumDbp / eveningCount * 10.0) / 10.0 : 0.0);
        stats.setHighestSbp(highestSbp);
        stats.setHighestDbp(highestDbp);
        stats.setLowestSbp(lowestSbp);
        stats.setLowestDbp(lowestDbp);
        stats.setHighBpCount(highBpCount);
        stats.setVeryHighBpCount(veryHighBpCount);
        
        return stats;
    }

    public AiSummaryResponse analyzePatientCondition(Integer patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return new AiSummaryResponse("Không tìm thấy bệnh nhân.");
        }
        Patient patient = patientOpt.get();
        
        String patientInfo = buildPatientContextString(patient);
        String adaStatsText = "";
        
        
        PatientAdaStats adaStats = calculateAdaStats(patientId);
        if (adaStats == null) {
            adaStatsText = "ĐƯỜNG HUYẾT: Dữ liệu đo đường huyết ngắt quãng, không đủ số lượng để phân tích chuẩn ADA (dưới 10 lần trong 14 ngày qua).";
        } else {
            adaStatsText = String.format(
                "ĐƯỜNG HUYẾT (14 ngày qua - %d lần đo): Mean: %.2f mmol/L, Min: %.2f, Max: %.2f. " +
                "TIR (3.9-10.0): %.1f%%, TAR (>10.0): %.1f%%, TBR (<3.9): %.1f%%. " +
                "Độ biến thiên (CV): %.1f%%. Chỉ số GMI ước tính: %.1f%%.",
                adaStats.getTotalReadings(), adaStats.getMeanGlucose(), adaStats.getMinGlucose(), adaStats.getMaxGlucose(),
                adaStats.getTir(), adaStats.getTar(), adaStats.getTbr(), adaStats.getCv(), adaStats.getGmi()
            );
        }

        String bpStatsText = "";
        PatientBpStats bpStats = calculateBpStats(patientId);
        if (bpStats == null) {
            bpStatsText = "HUYẾT ÁP: Dữ liệu đo huyết áp ngắt quãng, không đủ số lượng để phân tích chuẩn AHA (dưới 10 lần trong 14 ngày qua).";
        } else {
            bpStatsText = String.format(
                "HUYẾT ÁP (14 ngày qua - %d lần đo): Avg: %.1f/%.1f, Sáng: %.1f/%.1f, Tối: %.1f/%.1f. " +
                "Cao nhất: %d/%d, Thấp nhất: %d/%d. Số lần Huyết áp cao (>=140/90): %d, Báo động (>=180/120): %d.",
                bpStats.getTotalReadings(), bpStats.getAverageSbp(), bpStats.getAverageDbp(),
                bpStats.getMorningAvgSbp(), bpStats.getMorningAvgDbp(), bpStats.getEveningAvgSbp(), bpStats.getEveningAvgDbp(),
                bpStats.getHighestSbp(), bpStats.getHighestDbp(), bpStats.getLowestSbp(), bpStats.getLowestDbp(),
                bpStats.getHighBpCount(), bpStats.getVeryHighBpCount()
            );
        }
        
        // Lấy danh sách Alert chưa giải quyết từ hệ thống
        List<com.rpm.remotepatientmonitoring.model.Alert> unresolvedAlerts = alertRepository.findByPatientIdAndIsResolvedFalse(patientId);
        List<String> activeAlertMessages = new java.util.ArrayList<>();
        String currentAlertColor = "GREEN";

        for (com.rpm.remotepatientmonitoring.model.Alert alert : unresolvedAlerts) {
            if (alert.getAlertMessage() != null) {
                activeAlertMessages.add(alert.getAlertMessage());
            }
            String color = alert.getAlertColor();
            if ("RED".equalsIgnoreCase(color)) {
                currentAlertColor = "RED";
            } else if ("ORANGE".equalsIgnoreCase(color) && !"RED".equalsIgnoreCase(currentAlertColor)) {
                currentAlertColor = "ORANGE";
            } else if ("YELLOW".equalsIgnoreCase(color) && "GREEN".equalsIgnoreCase(currentAlertColor)) {
                currentAlertColor = "YELLOW";
            }
        }

        // Tự động nâng mức cảnh báo dựa trên chỉ số ADA/AHA nếu cần
        if (adaStats != null && (adaStats.getTbr() > 4.0 || adaStats.getTar() > 25.0)) {
            if (!"RED".equalsIgnoreCase(currentAlertColor)) currentAlertColor = "ORANGE";
        }
        if (bpStats != null) {
            if (bpStats.getVeryHighBpCount() > 0) {
                currentAlertColor = "RED";
            } else if (bpStats.getHighBpCount() > 3 && "GREEN".equalsIgnoreCase(currentAlertColor)) {
                currentAlertColor = "YELLOW";
            }
        }

        AiSummaryRequest request = new AiSummaryRequest();
        request.setPatientInfo(patientInfo);
        request.setAdaStatsText(adaStatsText);
        request.setBpStatsText(bpStatsText);
        
        try {
            ResponseEntity<AiSummaryResponse> response = restTemplate.postForEntity(
                "http://localhost:8000/api/summary", 
                request, 
                AiSummaryResponse.class
            );
            
            AiSummaryResponse summaryRes = response.getBody();
            if (summaryRes == null) {
                summaryRes = new AiSummaryResponse("Không nhận được phản hồi từ dịch vụ AI.");
            }

            summaryRes.setAlertColor(currentAlertColor);
            summaryRes.setActiveAlerts(activeAlertMessages);
            summaryRes.setAdaStats(adaStats);
            summaryRes.setBpStats(bpStats);

            // Lưu Audit Trail bằng bảng cũ (không đụng chạm cấu trúc DB của team)
            if (summaryRes.getClinicalSummary() != null) {
                AuditTrail audit = new AuditTrail();
                audit.setActorType("DOCTOR");
                audit.setActorId(0);
                audit.setAction("GENERATE_ADA_SUMMARY");
                audit.setTargetTable("PATIENT");
                audit.setTargetRecordId(patientId);
                audit.setOldValue(adaStatsText + " | " + bpStatsText); // Data gửi cho AI
                audit.setNewValue(summaryRes.getClinicalSummary()); // Báo cáo AI trả về
                auditTrailRepository.save(audit);

                // Lưu vào bảng chuyên dụng ai_clinical_summaries
                try {
                    com.rpm.remotepatientmonitoring.model.AiClinicalSummary summaryEntity = 
                        new com.rpm.remotepatientmonitoring.model.AiClinicalSummary(
                            patientId, 
                            null, 
                            summaryRes.getClinicalSummary()
                        );
                    aiClinicalSummaryRepository.save(summaryEntity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            return summaryRes;
        } catch (Exception e) {
            e.printStackTrace();
            AiSummaryResponse fallback = new AiSummaryResponse("Hệ thống AI đang bảo trì hoặc mất kết nối. Lỗi: " + e.getMessage());
            fallback.setAlertColor(currentAlertColor);
            fallback.setActiveAlerts(activeAlertMessages);
            fallback.setAdaStats(adaStats);
            fallback.setBpStats(bpStats);
            return fallback;
        }
    }
}
