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

        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctors);
        model.addAttribute("appointments", appointments);
        model.addAttribute("changeRequests", changeRequests);
        
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
}
