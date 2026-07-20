package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.dto.AiChatRequest;
import com.rpm.remotepatientmonitoring.dto.AiChatResponse;
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
import java.util.Optional;

@Service
public class AiChatService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_API_URL = "http://localhost:8000/api/chat";

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.DoctorRepository doctorRepository;

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
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            AiChatResponse errorResponse = new AiChatResponse();
            errorResponse.setAnswer("Xin lỗi, hệ thống AI hiện tại đang gặp sự cố. Vui lòng thử lại sau. (Lỗi: " + e.getMessage() + ")");
            return errorResponse;
        }
    }
}
