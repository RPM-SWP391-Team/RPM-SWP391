package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Appointment;
import com.rpm.remotepatientmonitoring.model.ChangeRequest;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AppointmentRepository;
import com.rpm.remotepatientmonitoring.repository.ChangeRequestRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import com.rpm.remotepatientmonitoring.repository.HealthLogRepository;
import com.rpm.remotepatientmonitoring.model.DailyHealthLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.rpm.remotepatientmonitoring.config.CustomUserDetails;
import com.rpm.remotepatientmonitoring.service.RatingService;
import com.rpm.remotepatientmonitoring.repository.DoctorRatingRepository;
import com.rpm.remotepatientmonitoring.model.DoctorRating;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.HashMap;

@Controller
@RequestMapping("/patient")
public class PatientInteractionController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ChangeRequestRepository changeRequestRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private DoctorRatingRepository doctorRatingRepository;

    @Autowired
    private com.rpm.remotepatientmonitoring.repository.NotificationRepository notificationRepository;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Optional<Patient> opt = patientRepository.findByAccountId(userDetails.getAccount().getId());
                if (opt.isPresent()) {
                    return opt.get();
                }
            }
        }
        List<Patient> all = patientRepository.findAll();
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    @GetMapping("/appointments")
    public String getAppointments(
            @RequestParam(value = "bpPage", defaultValue = "0") int bpPage,
            @RequestParam(value = "glucosePage", defaultValue = "0") int glucosePage,
            @RequestParam(value = "activeTab", defaultValue = "bp") String activeTab,
            @RequestParam(value = "filterRange", defaultValue = "all") String filterRange,
            @RequestParam(value = "filterDate", required = false) String filterDateStr,
            Model model) throws JsonProcessingException {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patient.getId());
        List<ChangeRequest> changeRequests = changeRequestRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());

        // Fetch logs for the last 7 days for the chart
        LocalDate chartStartDate = LocalDate.now().minusDays(7);
        List<DailyHealthLog> chartLogs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), chartStartDate);

        // Group and keep only the latest log per day and milestone for the chart, merging indices
        Map<String, DailyHealthLog> latestLogsMap = new LinkedHashMap<>();
        for (DailyHealthLog log : chartLogs) {
            String key = log.getLogDate().toString() + "_" + log.getLogType();
            DailyHealthLog existing = latestLogsMap.get(key);
            if (existing == null) {
                DailyHealthLog merged = new DailyHealthLog();
                merged.setLogDate(log.getLogDate());
                merged.setLogType(log.getLogType());
                merged.setSystolicBp(log.getSystolicBp());
                merged.setDiastolicBp(log.getDiastolicBp());
                merged.setHeartRate(log.getHeartRate());
                merged.setGlucoseLevel(log.getGlucoseLevel());
                merged.setLogTime(log.getLogTime());
                latestLogsMap.put(key, merged);
            } else {
                if (log.getSystolicBp() != null) {
                    existing.setSystolicBp(log.getSystolicBp());
                }
                if (log.getDiastolicBp() != null) {
                    existing.setDiastolicBp(log.getDiastolicBp());
                }
                if (log.getHeartRate() != null) {
                    existing.setHeartRate(log.getHeartRate());
                }
                if (log.getGlucoseLevel() != null) {
                    existing.setGlucoseLevel(log.getGlucoseLevel());
                }
                if (log.getLogTime().isAfter(existing.getLogTime())) {
                    existing.setLogTime(log.getLogTime());
                }
            }
        }

        List<String> dates = new ArrayList<>();
        List<Integer> systolicList = new ArrayList<>();
        List<Integer> diastolicList = new ArrayList<>();
        List<Double> glucoseList = new ArrayList<>();

        for (DailyHealthLog log : latestLogsMap.values()) {
            dates.add(log.getLogDate().toString() + " (" + log.getLogType() + ")");
            systolicList.add(log.getSystolicBp());
            diastolicList.add(log.getDiastolicBp());
            glucoseList.add(log.getGlucoseLevel() != null ? log.getGlucoseLevel().doubleValue() : null);
        }

        // --- Xử lý phân trang phía máy chủ (Server-side Pagination) ---
        LocalDate filterDate = null;
        if (filterDateStr != null && !filterDateStr.trim().isEmpty()) {
            try {
                filterDate = LocalDate.parse(filterDateStr);
            } catch (Exception e) {
                // ignore
            }
        }

        LocalDate rangeStart = null;
        LocalDate rangeEnd = null;
        if ("today".equals(filterRange)) {
            rangeStart = LocalDate.now();
            rangeEnd = LocalDate.now();
        } else if ("week".equals(filterRange)) {
            rangeStart = LocalDate.now().minusDays(7);
            rangeEnd = LocalDate.now();
        } else if ("month".equals(filterRange)) {
            rangeStart = LocalDate.now().minusDays(30);
            rangeEnd = LocalDate.now();
        }

        // Phân trang Huyết áp (systolicBp != null)
        Pageable bpPageable = PageRequest.of(bpPage, 5);
        Page<DailyHealthLog> bpPageObj;
        if (filterDate != null) {
            bpPageObj = healthLogRepository.findByPatientIdAndSystolicBpIsNotNullAndLogDateOrderByLogTimeDesc(patient.getId(), filterDate, bpPageable);
        } else if (rangeStart != null && rangeEnd != null) {
            bpPageObj = healthLogRepository.findByPatientIdAndSystolicBpIsNotNullAndLogDateBetweenOrderByLogTimeDesc(patient.getId(), rangeStart, rangeEnd, bpPageable);
        } else {
            bpPageObj = healthLogRepository.findByPatientIdAndSystolicBpIsNotNullOrderByLogTimeDesc(patient.getId(), bpPageable);
        }

        // Phân trang Đường huyết (glucoseLevel != null)
        Pageable glucosePageable = PageRequest.of(glucosePage, 5);
        Page<DailyHealthLog> glucosePageObj;
        if (filterDate != null) {
            glucosePageObj = healthLogRepository.findByPatientIdAndGlucoseLevelIsNotNullAndLogDateOrderByLogTimeDesc(patient.getId(), filterDate, glucosePageable);
        } else if (rangeStart != null && rangeEnd != null) {
            glucosePageObj = healthLogRepository.findByPatientIdAndGlucoseLevelIsNotNullAndLogDateBetweenOrderByLogTimeDesc(patient.getId(), rangeStart, rangeEnd, glucosePageable);
        } else {
            glucosePageObj = healthLogRepository.findByPatientIdAndGlucoseLevelIsNotNullOrderByLogTimeDesc(patient.getId(), glucosePageable);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        List<Doctor> doctors = doctorRepository.findAvailableDoctorsByHospital(patient.getHospital().getId());
        ratingService.populateDoctorRatings(doctors);

        // Fetch evaluated appointment IDs
        List<DoctorRating> patientRatings = doctorRatingRepository.findByPatientId(patient.getId());
        Set<Integer> evaluatedAppointmentIds = patientRatings.stream()
                .map(r -> r.getAppointment().getId())
                .collect(Collectors.toSet());

        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctors);
        model.addAttribute("appointments", appointments);
        model.addAttribute("changeRequests", changeRequests);
        model.addAttribute("evaluatedAppointmentIds", evaluatedAppointmentIds);
        
        model.addAttribute("bpLogs", bpPageObj.getContent());
        model.addAttribute("bpPageObj", bpPageObj);
        model.addAttribute("glucoseLogs", glucosePageObj.getContent());
        model.addAttribute("glucosePageObj", glucosePageObj);
        
        model.addAttribute("bpPage", bpPage);
        model.addAttribute("glucosePage", glucosePage);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("filterRange", filterRange);
        model.addAttribute("filterDate", filterDate != null ? filterDate.toString() : "");

        model.addAttribute("datesJson", objectMapper.writeValueAsString(dates));
        model.addAttribute("systolicJson", objectMapper.writeValueAsString(systolicList));
        model.addAttribute("diastolicJson", objectMapper.writeValueAsString(diastolicList));
        model.addAttribute("glucoseJson", objectMapper.writeValueAsString(glucoseList));

        return "patient/appointments";
    }

    @GetMapping("/request-change")
    public String requestChangePage(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        ChangeRequest changeRequest = ChangeRequest.builder()
                .status("PENDING")
                .build();

        model.addAttribute("patient", patient);
        model.addAttribute("changeRequest", changeRequest);
        return "patient/request-change";
    }

    @PostMapping("/request-change")
    public String submitChangeRequest(@ModelAttribute("changeRequest") ChangeRequest changeRequest) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        Doctor doctor = patient.getDoctor();
        if (doctor == null) {
            List<Doctor> all = doctorRepository.findAll();
            if (all.size() > 0) {
                doctor = all.get(0);
            } else {
                throw new IllegalStateException("No doctor found in database to receive requests.");
            }
        }

        changeRequest.setPatient(patient);
        changeRequest.setDoctor(doctor);
        changeRequest.setStatus("PENDING");
        changeRequest.setCreatedAt(LocalDateTime.now());
        changeRequest.setUpdatedAt(LocalDateTime.now());

        changeRequestRepository.save(changeRequest);

        // Notify Doctor
        if (doctor != null) {
            com.rpm.remotepatientmonitoring.model.Notification notif = com.rpm.remotepatientmonitoring.model.Notification.builder()
                    .doctor(doctor)
                    .recipientType("DOCTOR")
                    .title("Yêu cầu thay đổi mới")
                    .content("Bệnh nhân " + patient.getFullName() + " vừa gửi một yêu cầu " + 
                            (changeRequest.getRequestType().equals("RESCHEDULE") ? "đổi lịch khám" : "thay đổi phác đồ") + ".")
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
        }

        return "redirect:/patient/appointments?requestSuccess=true";
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(
            @RequestParam("appointmentTime") String appointmentTimeStr,
            @RequestParam("appointmentType") String appointmentType,
            @RequestParam("patientRequestReason") String patientRequestReason,
            @RequestParam("doctorId") Integer doctorId) {

        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isPresent() == false) {
            throw new IllegalArgumentException("Invalid doctor Id: " + doctorId);
        }
        Doctor doctor = doctorOpt.get();

        // Validate reason length
        if (patientRequestReason == null || patientRequestReason.trim().isEmpty()) {
            return "redirect:/patient/appointments?bookError=emptyReason";
        }
        if (patientRequestReason.length() > 500) {
            return "redirect:/patient/appointments?bookError=reasonTooLong";
        }

        // Chuyển chuỗi từ datetime-local sang LocalDateTime
        LocalDateTime apptTime;
        try {
            apptTime = LocalDateTime.parse(appointmentTimeStr);
        } catch (Exception e) {
            return "redirect:/patient/appointments?bookError=invalidDate";
        }

        // Validate date is in the future
        if (apptTime.isBefore(LocalDateTime.now())) {
            return "redirect:/patient/appointments?bookError=pastDate";
        }

        // Validate working hours (Monday-Friday, 08:00 to 17:00) if NOT emergency
        if (!"EMERGENCY".equals(appointmentType)) {
            java.time.DayOfWeek dayOfWeek = apptTime.getDayOfWeek();
            int hour = apptTime.getHour();
            if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                return "redirect:/patient/appointments?bookError=outsideWorkingHours";
            }
            if (hour < 8 || hour >= 17) {
                return "redirect:/patient/appointments?bookError=outsideWorkingHours";
            }
        }

        Appointment appt = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(apptTime)
                .appointmentType(appointmentType)
                .patientRequestReason(patientRequestReason)
                .status("PENDING")
                .createdBy("PATIENT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        appointmentRepository.save(appt);

        // Thông báo cho bác sĩ có lịch hẹn mới cần duyệt
        com.rpm.remotepatientmonitoring.model.Notification notif = com.rpm.remotepatientmonitoring.model.Notification.builder()
                .doctor(doctor)
                .patient(patient)
                .recipientType("DOCTOR")
                .title("Yêu cầu đặt lịch khám mới")
                .content("Bệnh nhân " + patient.getFullName() + " vừa đặt lịch khám vào "
                        + apptTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        + ". Vui lòng xem xét và xác nhận.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notif);

        return "redirect:/patient/appointments?bookSuccess=true";
    }

    @PostMapping("/appointments/delete/{id}")
    public String deleteAppointment(@PathVariable("id") Integer id) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        
        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        if (apptOpt.isPresent()) {
            Appointment appt = apptOpt.get();
            if (appt.getPatient() != null && appt.getPatient().getId().equals(patient.getId())) {
                if ("PENDING".equals(appt.getStatus())) {
                    appointmentRepository.delete(appt);
                }
            }
        }
        
        return "redirect:/patient/appointments?deleteSuccess=true";
    }

    @PostMapping("/request-change/delete/{id}")
    public String deleteChangeRequest(@PathVariable("id") Integer id) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        
        Optional<ChangeRequest> reqOpt = changeRequestRepository.findById(id);
        if (reqOpt.isPresent()) {
            ChangeRequest req = reqOpt.get();
            if (req.getPatient() != null && req.getPatient().getId().equals(patient.getId())) {
                if ("PENDING".equals(req.getStatus())) {
                    changeRequestRepository.delete(req);
                }
            }
        }
        
        return "redirect:/patient/appointments?deleteRequestSuccess=true";
    }

    // --- TRẮC NGHIỆM KIỂM TRA KIẾN THỨC ---
    public static class QuizQuestion {
        private int id;
        private String question;
        private List<String> options;
        private int correctOptionIndex;

        public QuizQuestion(int id, String question, List<String> options, int correctOptionIndex) {
            this.id = id;
            this.question = question;
            this.options = options;
            this.correctOptionIndex = correctOptionIndex;
        }

        public int getId() { return id; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public int getCorrectOptionIndex() { return correctOptionIndex; }
    }

    private static final List<QuizQuestion> QUESTION_BANK = List.of(
        new QuizQuestion(0, "Huyết áp là gì?", List.of("A. Lượng đường trong máu.", "B. Áp lực của máu tác động lên thành động mạch khi tim bơm máu.", "C. Số nhịp tim trong 1 phút.", "D. Lượng oxy trong máu."), 1),
        new QuizQuestion(1, "Trong kết quả huyết áp 120/80 mmHg, chỉ số 120 thể hiện điều gì?", List.of("A. Huyết áp tâm trương.", "B. Nhịp tim.", "C. Huyết áp tâm thu.", "D. Nồng độ oxy trong máu."), 2),
        new QuizQuestion(2, "Kết quả huyết áp 118/75 mmHg thuộc mức cảnh báo nào?", List.of("A. Xanh – An toàn", "B. Vàng – Chú ý", "C. Cam – Nguy hiểm", "D. Đỏ – Cấp cứu"), 0),
        new QuizQuestion(3, "Kết quả huyết áp 135/78 mmHg thuộc mức cảnh báo nào?", List.of("A. Xanh", "B. Vàng", "C. Cam", "D. Đỏ"), 1),
        new QuizQuestion(4, "Kết quả huyết áp 145/88 mmHg thuộc mức cảnh báo nào?", List.of("A. Xanh", "B. Vàng", "C. Cam", "D. Đỏ"), 2),
        new QuizQuestion(5, "Kết quả huyết áp 185/95 mmHg được phân loại là gì?", List.of("A. Xanh", "B. Vàng", "C. Cam", "D. Đỏ"), 3),
        new QuizQuestion(6, "Kết quả huyết áp 85/55 mmHg thuộc mức cảnh báo nào?", List.of("A. Xanh", "B. Vàng", "C. Cam (Hạ huyết áp)", "D. Đỏ"), 2),
        new QuizQuestion(7, "Theo nguyên tắc phân loại huyết áp, nếu hai chỉ số ở hai mức khác nhau thì phân loại theo mức nào?", List.of("A. Mức thấp hơn.", "B. Mức trung bình.", "C. Mức cao hơn.", "D. Lấy theo huyết áp tâm thu."), 2),
        new QuizQuestion(8, "Trước khi đo huyết áp, người bệnh nên nghỉ ngơi bao lâu?", List.of("A. 1–2 phút.", "B. 5–10 phút.", "C. 20 phút.", "D. 30 phút."), 1),
        new QuizQuestion(9, "Điều nào KHÔNG nên làm trước khi đo huyết áp?", List.of("A. Nghỉ ngơi.", "B. Hút thuốc trong vòng 2 giờ trước khi đo.", "C. Ngồi thư giãn.", "D. Để cơ thể thả lỏng."), 1),
        new QuizQuestion(10, "Tư thế đúng khi đo huyết áp là gì?", List.of("A. Ngồi bắt chéo chân.", "B. Cánh tay thấp hơn tim.", "C. Hai chân đặt trên sàn, cánh tay ngang mức tim.", "D. Đứng và cầm máy trên tay."), 2),
        new QuizQuestion(11, "Lần đầu đo huyết áp nên thực hiện như thế nào?", List.of("A. Chỉ đo tay trái.", "B. Chỉ đo tay phải.", "C. Đo cả hai tay.", "D. Đo tay thuận."), 2),
        new QuizQuestion(12, "Nếu hai lần đo huyết áp chênh nhau trên 10 mmHg thì nên làm gì?", List.of("A. Lấy kết quả cao hơn.", "B. Lấy kết quả thấp hơn.", "C. Nghỉ trên 5 phút rồi đo lại.", "D. Không cần đo lại."), 2),
        new QuizQuestion(13, "Dấu hiệu nào dưới đây có thể gặp khi tăng huyết áp đột ngột?", List.of("A. Đau đầu dữ dội.", "B. Run tay.", "C. Đói cồn cào.", "D. Vã mồ hôi lạnh."), 0),
        new QuizQuestion(14, "Người bệnh có huyết áp ≥180/120 mmHg kèm khó nói và yếu tay chân cần xử trí như thế nào?", List.of("A. Chờ 1 ngày rồi đo lại.", "B. Tự uống nhiều nước.", "C. Gọi cấp cứu hoặc đưa đến bệnh viện ngay.", "D. Tự ý ngậm thuốc hạ huyết áp tác dụng nhanh."), 2),
        new QuizQuestion(15, "Theo cẩm nang, mức đường huyết bình thường là bao nhiêu?", List.of("A. <4,4 mmol/L.", "B. 4,4–10 mmol/L.", "C. 10–16 mmol/L.", "D. >16 mmol/L."), 1),
        new QuizQuestion(16, "Đường huyết dưới 4,4 mmol/L được xếp vào mức nào?", List.of("A. Bình thường.", "B. Tăng đường huyết.", "C. Hạ đường huyết.", "D. Khẩn cấp do tăng đường huyết."), 2),
        new QuizQuestion(17, "Triệu chứng nào thường gặp khi đường huyết tăng cao?", List.of("A. Khát nước nhiều và tiểu nhiều.", "B. Run tay và đói cồn cào.", "C. Chảy máu cam.", "D. Liệt mặt."), 0),
        new QuizQuestion(18, "Khi người bệnh còn tỉnh và bị hạ đường huyết, nên làm gì đầu tiên?", List.of("A. Tiêm thêm insulin.", "B. Uống ngay nước đường hoặc đồ uống có đường.", "C. Không ăn uống gì.", "D. Đi ngủ."), 1),
        new QuizQuestion(19, "Điều nào sau đây là đúng khi phòng ngừa tăng và hạ đường huyết?", List.of("A. Tự ý tăng hoặc giảm thuốc khi thấy khỏe hơn.", "B. Chỉ đo đường huyết khi có triệu chứng.", "C. Dùng thuốc đúng chỉ định, ăn uống hợp lý và tập luyện đều đặn.", "D. Chỉ cần kiêng đồ ngọt là đủ."), 2)
    );

    @GetMapping("/quiz")
    public String getQuiz(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("patient", patient);
        model.addAttribute("questions", QUESTION_BANK);
        model.addAttribute("isSubmitted", false);
        return "patient/quiz";
    }

    @PostMapping("/quiz/submit")
    public String submitQuiz(
            @RequestParam("questionIds") List<Integer> questionIds,
            @RequestParam Map<String, String> allParams,
            Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        List<QuizQuestion> questions = new ArrayList<>();
        Map<Integer, Integer> userAnswers = new HashMap<>();
        int score = 0;

        for (Integer qId : questionIds) {
            QuizQuestion qq = QUESTION_BANK.stream().filter(q -> q.getId() == qId).findFirst().orElse(null);
            if (qq != null) {
                questions.add(qq);
                String answerStr = allParams.get("answer_" + qId);
                Integer userAnswer = (answerStr != null) ? Integer.parseInt(answerStr) : -1;
                userAnswers.put(qId, userAnswer);
                if (userAnswer == qq.getCorrectOptionIndex()) {
                    score++;
                }
            }
        }

        model.addAttribute("patient", patient);
        model.addAttribute("questions", questions);
        model.addAttribute("userAnswers", userAnswers);
        model.addAttribute("score", score);
        model.addAttribute("isSubmitted", true);
        return "patient/quiz";
    }
}
