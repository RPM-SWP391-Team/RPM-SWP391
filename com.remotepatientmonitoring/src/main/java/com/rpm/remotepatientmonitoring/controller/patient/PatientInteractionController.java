package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Appointment;
import com.rpm.remotepatientmonitoring.model.ChangeRequest;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
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
import com.rpm.remotepatientmonitoring.service.patient.PatientHealthService;
import com.rpm.remotepatientmonitoring.service.patient.PatientInteractionService;
import com.rpm.remotepatientmonitoring.repository.ChangeRequestRepository;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;

@Controller
@RequestMapping("/patient")
public class PatientInteractionController {

    @Autowired
    private PatientInteractionService patientInteractionService;

    @Autowired
    private PatientHealthService patientHealthService;

    @Autowired
    private RatingService ratingService;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Patient patient = patientHealthService.getPatientByAccountId(userDetails.getAccount().getId());
                if (patient != null) {
                    return patient;
                }
            }
        }
        return null;
    }

    @GetMapping("/appointments")
    public String getAppointments(
            @RequestParam(value = "bpPage", defaultValue = "0") int bpPage,
            @RequestParam(value = "glucosePage", defaultValue = "0") int glucosePage,
            @RequestParam(value = "activeTab", defaultValue = "bp") String activeTab,
            @RequestParam(value = "filterRange", defaultValue = "all") String filterRange,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            Model model) throws JsonProcessingException {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        List<Appointment> appointments = patientInteractionService.getAppointments(patient.getId());
        List<ChangeRequest> changeRequests = patientInteractionService.getChangeRequests(patient.getId());

        // Lấy nhật ký hoạt động 7 ngày gần đây cho biểu đồ
        LocalDate chartStartDate = LocalDate.now().minusDays(7); // Ngày hiện tại - 7 ngày
        List<DailyHealthLog> chartLogs = patientInteractionService.getLatestLogsForChart(patient.getId(), chartStartDate);

        // Nhóm lại và chỉ giữ lại nhật ký mới nhất mỗi ngày và mốc thời gian cho biểu đồ, hợp nhất các chỉ số
        Map<String, DailyHealthLog> latestLogsMap = new LinkedHashMap<>();
        for (DailyHealthLog log : chartLogs) {
            String key = log.getLogDate().toString() + "_" + log.getLogType(); // Ví dụ: "2026-07-27_EVENING"
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
            dates.add(log.getLogDate().toString() + " (" + log.getLogType() + ")"); // Ví dụ: 2026-07-23 (MORNING)
            systolicList.add(log.getSystolicBp());
            diastolicList.add(log.getDiastolicBp());
            glucoseList.add(log.getGlucoseLevel() != null ? log.getGlucoseLevel().doubleValue() : null); // Chuyển BigDecimal -> double
        }

        // --- Xử lý lọc Từ ngày - Đến ngày ---
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try { startDate = LocalDate.parse(startDateStr.trim()); } catch (Exception ignored) {}
        }
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try { endDate = LocalDate.parse(endDateStr.trim()); } catch (Exception ignored) {}
        }

        // Phân trang Huyết áp (systolicBp != null)
        Pageable bpPageable = PageRequest.of(bpPage, 5);
        Page<DailyHealthLog> bpPageObj = patientInteractionService.getBpLogsPageWithRange(patient.getId(), filterRange, startDate, endDate, bpPageable);

        // Phân trang Đường huyết (glucoseLevel != null)
        Pageable glucosePageable = PageRequest.of(glucosePage, 5);
        Page<DailyHealthLog> glucosePageObj = patientInteractionService.getGlucoseLogsPageWithRange(patient.getId(), filterRange, startDate, endDate, glucosePageable);

        ObjectMapper objectMapper = new ObjectMapper();

        List<Doctor> doctors = (patient.getHospital() != null) 
                ? patientInteractionService.getAvailableDoctors(patient.getHospital().getId()) 
                : new ArrayList<>();
        ratingService.populateDoctorRatings(doctors);

        // Fetch evaluated appointment IDs
        Set<Integer> evaluatedAppointmentIds = ratingService.getEvaluatedAppointmentIdsByPatientId(patient.getId());

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
        model.addAttribute("startDate", startDateStr != null ? startDateStr : "");
        model.addAttribute("endDate", endDateStr != null ? endDateStr : "");

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

        if (changeRequest.getPatientReason() == null || changeRequest.getPatientReason().trim().isEmpty()) {
            return "redirect:/patient/request-change?createError=emptyReason";
        }
        if (changeRequest.getPatientReason().length() > 500) {
            return "redirect:/patient/request-change?createError=reasonTooLong";
        }
        if (changeRequest.getRequestType() == null || changeRequest.getRequestType().trim().isEmpty()) {
            return "redirect:/patient/request-change?createError=emptyType";
        }

        try {
            patientInteractionService.createChangeRequest(changeRequest, patient);
        } catch (IllegalStateException ex) {
            return "redirect:/patient/request-change?createError=noDoctor";
        }

        return "redirect:/patient/appointments?requestSuccess=true";
    }

    @GetMapping("/appointments/book")
    public String showBookAppointmentForm(Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        List<Doctor> doctors = patientInteractionService.getAvailableDoctors(patient.getHospital().getId());
        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctors);
        return "patient/book-appointment";
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

        // Validate reason length
        if (patientRequestReason == null || patientRequestReason.trim().isEmpty()) {
            return "redirect:/patient/appointments/book?bookError=emptyReason";
        }
        if (patientRequestReason.length() > 500) {
            return "redirect:/patient/appointments/book?bookError=reasonTooLong";
        }

        // Chuyển chuỗi từ datetime-local sang LocalDateTime
        LocalDateTime apptTime;
        try {
            apptTime = LocalDateTime.parse(appointmentTimeStr);
        } catch (Exception e) {
            return "redirect:/patient/appointments/book?bookError=invalidDate";
        }

        // Validate date is in the future
        if (apptTime.isBefore(LocalDateTime.now())) {
            return "redirect:/patient/appointments/book?bookError=pastDate";
        }

        // Validate working hours (Monday-Friday, 08:00 to 17:00) if NOT emergency
        if (!"EMERGENCY".equals(appointmentType)) {
            java.time.DayOfWeek dayOfWeek = apptTime.getDayOfWeek();
            int hour = apptTime.getHour();
            if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                return "redirect:/patient/appointments/book?bookError=outsideWorkingHours";
            }
            if (hour < 8 || hour >= 17) {
                return "redirect:/patient/appointments/book?bookError=outsideWorkingHours";
            }
        }

        try {
            patientInteractionService.bookAppointment(patient, doctorId, appointmentType, patientRequestReason, apptTime);
        } catch (IllegalArgumentException ex) {
            return "redirect:/patient/appointments/book?bookError=invalidDoctor";
        }

        return "redirect:/patient/appointments?bookSuccess=true";
    }

    @GetMapping("/appointments/edit/{id}")
    public String editAppointmentPage(@PathVariable("id") Integer id, Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        Appointment appt = patientInteractionService.getAppointmentById(id);
        if (appt == null || appt.getPatient() == null || !appt.getPatient().getId().equals(patient.getId())) {
            return "redirect:/patient/appointments";
        }

        List<Doctor> doctors = new ArrayList<>();
        if (patient.getHospital() != null) {
            List<Doctor> hospDocs = patientInteractionService.getAvailableDoctors(patient.getHospital().getId());
            if (hospDocs != null) {
                doctors.addAll(hospDocs);
            }
        }

        if (doctors.isEmpty()) {
            List<Doctor> allDocs = patientInteractionService.getAllDoctors();
            if (allDocs != null) {
                doctors.addAll(allDocs);
            }
        }

        if (appt.getDoctor() != null) {
            boolean alreadyInList = false;
            for (Doctor d : doctors) {
                if (d != null && d.getId() != null && d.getId().equals(appt.getDoctor().getId())) {
                    alreadyInList = true;
                    break;
                }
            }
            if (!alreadyInList) {
                doctors.add(0, appt.getDoctor());
            }
        }

        model.addAttribute("patient", patient);
        model.addAttribute("appointment", appt);
        model.addAttribute("doctors", doctors);
        return "patient/edit-appointment";
    }

    @PostMapping("/appointments/update/{id}")
    public String updateAppointment(
            @PathVariable("id") Integer id,
            @RequestParam("appointmentTime") String appointmentTimeStr,
            @RequestParam("appointmentType") String appointmentType,
            @RequestParam("patientRequestReason") String patientRequestReason,
            @RequestParam("doctorId") Integer doctorId) {

        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        // Validate reason length
        if (patientRequestReason == null || patientRequestReason.trim().isEmpty()) {
            return "redirect:/patient/appointments?updateError=emptyReason";
        }
        if (patientRequestReason.length() > 500) {
            return "redirect:/patient/appointments?updateError=reasonTooLong";
        }

        // Chuyển chuỗi từ datetime-local sang LocalDateTime
        LocalDateTime apptTime;
        try {
            apptTime = LocalDateTime.parse(appointmentTimeStr);
        } catch (Exception e) {
            return "redirect:/patient/appointments?updateError=invalidDate";
        }

        // Validate date is in the future
        if (apptTime.isBefore(LocalDateTime.now())) {
            return "redirect:/patient/appointments?updateError=pastDate";
        }

        // Validate working hours (Monday-Friday, 08:00 to 17:00) if NOT emergency
        if (!"EMERGENCY".equals(appointmentType)) {
            java.time.DayOfWeek dayOfWeek = apptTime.getDayOfWeek();
            int hour = apptTime.getHour();
            if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                return "redirect:/patient/appointments?updateError=outsideWorkingHours";
            }
            if (hour < 8 || hour >= 17) {
                return "redirect:/patient/appointments?updateError=outsideWorkingHours";
            }
        }

        try {
            patientInteractionService.updateAppointment(id, patient.getId(), doctorId, appointmentType, patientRequestReason, apptTime);
        } catch (IllegalArgumentException ex) {
            return "redirect:/patient/appointments?updateError=invalidDoctor";
        } catch (IllegalStateException ex) {
            return "redirect:/patient/appointments?updateError=notPending";
        }

        return "redirect:/patient/appointments?updateSuccess=true";
    }

    @PostMapping("/appointments/delete/{id}")
    public String deleteAppointment(@PathVariable("id") Integer id) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        try {
            patientInteractionService.deleteAppointment(id, patient.getId());
            return "redirect:/patient/appointments?deleteSuccess=true";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return "redirect:/patient/appointments?deleteRequestSuccess=true";
        }
    }

    @PostMapping("/request-change/delete/{id}")
    public String deleteChangeRequest(@PathVariable("id") Integer id) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        try {
            patientInteractionService.deleteChangeRequest(id, patient.getId());
            return "redirect:/patient/appointments?deleteSuccess=true";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return "redirect:/patient/appointments?deleteRequestSuccess=true";
        }
    }

    @GetMapping("/request-change/edit/{id}")
    public String editRequestChangePage(@PathVariable("id") Integer id, Model model) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        Optional<ChangeRequest> reqOpt = patientInteractionService.findChangeRequestById(id);
        if (reqOpt.isEmpty()) {
            return "redirect:/patient/appointments";
        }
        ChangeRequest changeRequest = reqOpt.get();
        if (!changeRequest.getPatient().getId().equals(patient.getId())) {
            return "redirect:/patient/appointments";
        }
        if (!"PENDING".equals(changeRequest.getStatus())) {
            return "redirect:/patient/appointments";
        }

        model.addAttribute("patient", patient);
        model.addAttribute("changeRequest", changeRequest);
        return "patient/edit-change-request";
    }

    @PostMapping("/request-change/update/{id}")
    public String updateChangeRequest(
            @PathVariable("id") Integer id,
            @RequestParam("requestType") String requestType,
            @RequestParam("patientReason") String patientReason) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        if (patientReason == null || patientReason.trim().isEmpty()) {
            return "redirect:/patient/request-change/edit/" + id + "?editError=emptyReason";
        }
        if (patientReason.length() > 500) {
            return "redirect:/patient/request-change/edit/" + id + "?editError=reasonTooLong";
        }

        try {
            patientInteractionService.updateChangeRequest(id, requestType, patientReason, patient.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/patient/appointments?updateRequestError=true";
        }

        return "redirect:/patient/appointments?updateRequestSuccess=true";
    }
}
