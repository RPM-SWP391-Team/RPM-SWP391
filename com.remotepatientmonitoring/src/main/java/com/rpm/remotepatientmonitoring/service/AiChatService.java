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
                    } else {
                        docContext.append("Bác sĩ đang ở Dashboard tổng quan, tư vấn và trả lời phác đồ y khoa tổng quát.");
                    }

                    // Nạp 3 câu thoại gần nhất làm bộ nhớ ngữ cảnh hội thoại đa lượt (Multi-turn Conversation Memory)
                    try {
                        List<com.rpm.remotepatientmonitoring.model.AiChatHistory> recentChats;
                        if (patientId != null) {
                            recentChats = aiChatHistoryRepository.findTop20ByPatientIdOrderByCreatedAtDesc(patientId);
                        } else {
                            recentChats = aiChatHistoryRepository.findTop20ByAccountIdAndPatientIdIsNullOrderByCreatedAtDesc(account.getId());
                        }

                        if (recentChats != null && !recentChats.isEmpty()) {
                            // Lọc các câu thoại lỗi "trang Dashboard" nếu đang ở trong hồ sơ bệnh nhân
                            List<com.rpm.remotepatientmonitoring.model.AiChatHistory> validChats = new java.util.ArrayList<>();
                            for (com.rpm.remotepatientmonitoring.model.AiChatHistory h : recentChats) {
                                String ans = h.getAnswer() != null ? h.getAnswer() : "";
                                if (patientId != null && ans.contains("trang Dashboard")) {
                                    continue; // Bỏ qua câu trả lời lỗi bị nhiễm từ trước
                                }
                                validChats.add(h);
                            }

                            if (!validChats.isEmpty()) {
                                docContext.append("\n[Lịch sử hội thoại 3 câu gần nhất]: ");
                                int limit = Math.min(validChats.size(), 3);
                                List<com.rpm.remotepatientmonitoring.model.AiChatHistory> subList = new java.util.ArrayList<>(validChats.subList(0, limit));
                                java.util.Collections.reverse(subList);
                                for (com.rpm.remotepatientmonitoring.model.AiChatHistory h : subList) {
                                    String cleanAns = h.getAnswer().replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
                                    String shortAns = cleanAns.length() > 120 ? cleanAns.substring(0, 120) + "..." : cleanAns;
                                    docContext.append(String.format("(Hỏi: %s | AI trả lời: %s) ", h.getQuestion(), shortAns));
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
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
        String pName = patient.getFullName() != null ? patient.getFullName() : ("Bệnh nhân " + patient.getId());
        String pCode = patient.getPatientCode() != null ? patient.getPatientCode() : ("PAT" + patient.getId());
        
        context.append(String.format("Hồ sơ Bệnh nhân: %s (Mã: %s), Giới tính: %s, %d tuổi. Trạng thái: %s. ", 
            pName, pCode, genderStr, age, patient.getStatus() != null ? patient.getStatus() : "Đang điều trị"));
        
        // Bệnh lý nền
        if (patient.getDiseaseProfile() != null) {
            context.append("Bệnh lý nền: ").append(patient.getDiseaseProfile().getProfileName()).append(". ");
        }
        
        // Phác đồ điều trị hiện tại
        Optional<TreatmentPlan> planOpt = treatmentPlanRepository.findByPatientIdAndIsCurrent(patient.getId(), true);
        if (planOpt.isPresent()) {
            TreatmentPlan plan = planOpt.get();
            if (plan.getMedicalOrder() != null && !plan.getMedicalOrder().isEmpty()) {
                context.append("Thuốc điều trị hiện tại: ").append(plan.getMedicalOrder().replace("\n", " ")).append(". ");
            }
            if (plan.getExerciseGoal() != null && !plan.getExerciseGoal().isEmpty()) {
                context.append("Chỉ định sinh hoạt/tập luyện: ").append(plan.getExerciseGoal().replace("\n", " ")).append(". ");
            }
        }

        // Tích hợp Thống kê 14 ngày gần nhất chuẩn ADA/AHA vào ngữ cảnh Chatbot
        try {
            PatientAdaStats adaStats = calculateAdaStats(patient.getId(), null, null);
            if (adaStats != null) {
                context.append(String.format(
                    "[Thống kê Đường huyết 14 ngày gần nhất - %d lần đo]: Trung bình: %.2f mmol/L (Min: %.2f, Max: %.2f), " +
                    "TIR (Trong mục tiêu 3.9-10.0): %.1f%%, TAR (Cao >10.0): %.1f%%, TBR (Hạ <3.9): %.1f%%, Độ biến thiên CV: %.1f%%, GMI: %.1f%%. ",
                    adaStats.getTotalReadings(), adaStats.getMeanGlucose(), adaStats.getMinGlucose(), adaStats.getMaxGlucose(),
                    adaStats.getTir(), adaStats.getTar(), adaStats.getTbr(), adaStats.getCv(), adaStats.getGmi()
                ));
            } else {
                context.append("[Thống kê Đường huyết 14 ngày gần nhất]: Chưa có nhật ký đo đường huyết. ");
            }

            PatientBpStats bpStats = calculateBpStats(patient.getId(), null, null);
            if (bpStats != null) {
                context.append(String.format(
                    "[Thống kê Huyết áp 14 ngày gần nhất - %d lần đo]: Trung bình: %.1f/%.1f mmHg (Sáng: %.1f/%.1f, Tối: %.1f/%.1f), " +
                    "Cao nhất: %d/%d, Thấp nhất: %d/%d, Số lần Huyết áp cao (>=140/90): %d, Báo động (>=180/120): %d. ",
                    bpStats.getTotalReadings(), bpStats.getAverageSbp(), bpStats.getAverageDbp(),
                    bpStats.getMorningAvgSbp(), bpStats.getMorningAvgDbp(), bpStats.getEveningAvgSbp(), bpStats.getEveningAvgDbp(),
                    bpStats.getHighestSbp(), bpStats.getHighestDbp(), bpStats.getLowestSbp(), bpStats.getLowestDbp(),
                    bpStats.getHighBpCount(), bpStats.getVeryHighBpCount()
                ));
            } else {
                context.append("[Thống kê Huyết áp 14 ngày gần nhất]: Chưa có nhật ký đo huyết áp. ");
            }

            // Cảnh báo y tế chưa xử lý
            List<com.rpm.remotepatientmonitoring.model.Alert> unresolvedAlerts = alertRepository.findByPatientIdAndIsResolvedFalse(patient.getId());
            if (unresolvedAlerts != null && !unresolvedAlerts.isEmpty()) {
                context.append("[Cảnh báo y tế đang chờ xử lý]: ");
                for (com.rpm.remotepatientmonitoring.model.Alert alert : unresolvedAlerts) {
                    context.append(String.format("(%s: %s, Giá trị: %s) ", alert.getMetricType(), alert.getAlertMessage(), alert.getMetricValue()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return context.toString();
    }

    public AiChatResponse getChatbotResponse(String email, Integer patientId, String question, String userContext) {
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

        AiChatRequest request = new AiChatRequest(email, patientId, question, userContext);
        
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
                                request.getPatientId(), 
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

    public List<com.rpm.remotepatientmonitoring.model.AiChatHistory> getChatHistory(String email, Integer patientId) {
        try {
            if (patientId != null) {
                return aiChatHistoryRepository.findTop20ByPatientIdOrderByCreatedAtDesc(patientId);
            }
            Optional<Account> accountOpt = accountRepository.findByEmail(email);
            if (accountOpt.isPresent()) {
                return aiChatHistoryRepository.findTop20ByAccountIdAndPatientIdIsNullOrderByCreatedAtDesc(accountOpt.get().getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return java.util.Collections.emptyList();
    }

    public void clearChatHistory(String email, Integer patientId) {
        try {
            if (patientId != null) {
                aiChatHistoryRepository.deleteByPatientId(patientId);
            } else {
                Optional<Account> accountOpt = accountRepository.findByEmail(email);
                if (accountOpt.isPresent()) {
                    aiChatHistoryRepository.deleteByAccountIdAndPatientIdIsNull(accountOpt.get().getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        return calculateAdaStats(patientId, null, null);
    }

    public PatientAdaStats calculateAdaStats(Integer patientId, LocalDate customStartDate) {
        return calculateAdaStats(patientId, customStartDate, null);
    }

    public PatientAdaStats calculateAdaStats(Integer patientId, LocalDate customStartDate, LocalDate customEndDate) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = customStartDate;
        LocalDate endDate = customEndDate;
        if (startDate != null && startDate.isAfter(today)) startDate = today;
        if (endDate != null && endDate.isAfter(today)) endDate = today;

        if (startDate == null && endDate == null) {
            endDate = today;
            startDate = endDate.minusDays(14);
        } else if (startDate != null && endDate == null) {
            endDate = startDate.plusDays(14);
            if (endDate.isAfter(today)) endDate = today;
        } else if (startDate == null && endDate != null) {
            startDate = endDate.minusDays(14);
        }

        List<DailyHealthLog> logs = healthLogRepository.findByPatientIdAndLogDateBetweenOrderByLogDateAsc(patientId, startDate, endDate);
        
        List<DailyHealthLog> glucoseLogs = logs.stream()
                .filter(log -> log.getGlucoseLevel() != null)
                .collect(java.util.stream.Collectors.toList());
                
        if (glucoseLogs.isEmpty()) {
            return null; // No glucose data
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
        return calculateBpStats(patientId, null, null);
    }

    public PatientBpStats calculateBpStats(Integer patientId, LocalDate customStartDate) {
        return calculateBpStats(patientId, customStartDate, null);
    }

    public PatientBpStats calculateBpStats(Integer patientId, LocalDate customStartDate, LocalDate customEndDate) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = customStartDate;
        LocalDate endDate = customEndDate;
        if (startDate != null && startDate.isAfter(today)) startDate = today;
        if (endDate != null && endDate.isAfter(today)) endDate = today;

        if (startDate == null && endDate == null) {
            endDate = today;
            startDate = endDate.minusDays(14);
        } else if (startDate != null && endDate == null) {
            endDate = startDate.plusDays(14);
            if (endDate.isAfter(today)) endDate = today;
        } else if (startDate == null && endDate != null) {
            startDate = endDate.minusDays(14);
        }

        List<DailyHealthLog> logs = healthLogRepository.findByPatientIdAndLogDateBetweenOrderByLogDateAsc(patientId, startDate, endDate);
        
        List<DailyHealthLog> bpLogs = logs.stream()
                .filter(log -> log.getSystolicBp() != null && log.getDiastolicBp() != null)
                .collect(java.util.stream.Collectors.toList());
                
        if (bpLogs.isEmpty()) {
            return null; // No BP data
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
            
            if (log.getLogTime() != null) {
                int hour = log.getLogTime().getHour();
                if (hour >= 5 && hour <= 11) {
                    morningSumSbp += sbp;
                    morningSumDbp += dbp;
                    morningCount++;
                } else if (hour >= 17 && hour <= 23) {
                    eveningSumSbp += sbp;
                    eveningSumDbp += dbp;
                    eveningCount++;
                }
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
        return analyzePatientCondition(patientId, null, null);
    }

    public AiSummaryResponse analyzePatientCondition(Integer patientId, LocalDate customStartDate) {
        return analyzePatientCondition(patientId, customStartDate, null);
    }

    public AiSummaryResponse analyzePatientCondition(Integer patientId, LocalDate customStartDate, LocalDate customEndDate) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return new AiSummaryResponse("Không tìm thấy bệnh nhân.");
        }
        Patient patient = patientOpt.get();
        
        LocalDate today = LocalDate.now();
        LocalDate startDate = customStartDate;
        LocalDate endDate = customEndDate;
        if (startDate != null && startDate.isAfter(today)) startDate = today;
        if (endDate != null && endDate.isAfter(today)) endDate = today;

        if (startDate == null && endDate == null) {
            endDate = today;
            startDate = endDate.minusDays(14);
        } else if (startDate != null && endDate == null) {
            endDate = startDate.plusDays(14);
            if (endDate.isAfter(today)) endDate = today;
        } else if (startDate == null && endDate != null) {
            startDate = endDate.minusDays(14);
        }

        String periodLabel = String.format("Giai đoạn (%s đến %s)", 
            startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
            endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String patientInfo = buildPatientContextString(patient) + " [Phân tích " + periodLabel + "]";
        String adaStatsText = "";
        
        PatientAdaStats adaStats = calculateAdaStats(patientId, startDate, endDate);
        if (adaStats == null) {
            adaStatsText = "ĐƯỜNG HUYẾT: Không có dữ liệu đo đường huyết trong " + periodLabel + ".";
        } else {
            adaStatsText = String.format(
                "ĐƯỜNG HUYẾT (%s - %d lần đo): Mean: %.2f mmol/L, Min: %.2f, Max: %.2f. " +
                "TIR (3.9-10.0): %.1f%%, TAR (>10.0): %.1f%%, TBR (<3.9): %.1f%%. " +
                "Độ biến thiên (CV): %.1f%%. Chỉ số GMI ước tính: %.1f%%.",
                periodLabel, adaStats.getTotalReadings(), adaStats.getMeanGlucose(), adaStats.getMinGlucose(), adaStats.getMaxGlucose(),
                adaStats.getTir(), adaStats.getTar(), adaStats.getTbr(), adaStats.getCv(), adaStats.getGmi()
            );
        }

        String bpStatsText = "";
        PatientBpStats bpStats = calculateBpStats(patientId, startDate, endDate);
        if (bpStats == null) {
            bpStatsText = "HUYẾT ÁP: Không có dữ liệu đo huyết áp trong " + periodLabel + ".";
        } else {
            bpStatsText = String.format(
                "HUYẾT ÁP (%s - %d lần đo): Avg: %.1f/%.1f, Sáng: %.1f/%.1f, Tối: %.1f/%.1f. " +
                "Cao nhất: %d/%d, Thấp nhất: %d/%d. Số lần Huyết áp cao (>=140/90): %d, Báo động (>=180/120): %d.",
                periodLabel, bpStats.getTotalReadings(), bpStats.getAverageSbp(), bpStats.getAverageDbp(),
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
