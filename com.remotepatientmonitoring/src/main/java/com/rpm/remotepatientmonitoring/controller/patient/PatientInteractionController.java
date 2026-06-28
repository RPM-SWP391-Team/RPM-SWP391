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

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

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
    public String getAppointments(Model model) throws JsonProcessingException {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }

        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patient.getId());
        List<ChangeRequest> changeRequests = changeRequestRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());

        // Fetch logs for the last 7 days for the chart
        LocalDate startDate = LocalDate.now().minusDays(7);
        List<DailyHealthLog> chartLogs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);

        // Group and keep only the latest log per day and milestone for the chart
        Map<String, DailyHealthLog> latestLogsMap = new LinkedHashMap<>();
        for (DailyHealthLog log : chartLogs) {
            String key = log.getLogDate().toString() + "_" + log.getLogType();
            DailyHealthLog existing = latestLogsMap.get(key);
            if (existing == null || log.getLogTime().isAfter(existing.getLogTime())) {
                latestLogsMap.put(key, log);
            }
        }

        List<String> dates = new ArrayList<>();
        List<Integer> systolicList = new ArrayList<>();
        List<Integer> diastolicList = new ArrayList<>();
        List<Double> glucoseList = new ArrayList<>();

        for (DailyHealthLog log : latestLogsMap.values()) {
            dates.add(log.getLogDate().toString() + " (" + log.getLogType() + ")");
            systolicList.add(log.getSystolicBp() != null ? log.getSystolicBp() : 0);
            diastolicList.add(log.getDiastolicBp() != null ? log.getDiastolicBp() : 0);
            glucoseList.add(log.getGlucoseLevel() != null ? log.getGlucoseLevel().doubleValue() : 0.0);
        }

        // Fetch all raw logs ordered by time desc for the history list
        List<DailyHealthLog> rawHistoryLogs = healthLogRepository.findByPatientIdOrderByLogTimeDesc(patient.getId());

        ObjectMapper objectMapper = new ObjectMapper();

        List<Doctor> doctors = doctorRepository.findAvailableDoctorsByHospital(patient.getHospital().getId());

        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctors);
        model.addAttribute("appointments", appointments);
        model.addAttribute("changeRequests", changeRequests);
        model.addAttribute("healthLogs", rawHistoryLogs); // Full raw logs list
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

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid doctor Id: " + doctorId));

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
        
        appointmentRepository.findById(id).ifPresent(appt -> {
            if (appt.getPatient() != null && appt.getPatient().getId().equals(patient.getId())) {
                if ("PENDING".equals(appt.getStatus())) {
                    appointmentRepository.delete(appt);
                }
            }
        });
        
        return "redirect:/patient/appointments?deleteSuccess=true";
    }

    @PostMapping("/request-change/delete/{id}")
    public String deleteChangeRequest(@PathVariable("id") Integer id) {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return "redirect:/auth/login";
        }
        
        changeRequestRepository.findById(id).ifPresent(req -> {
            if (req.getPatient() != null && req.getPatient().getId().equals(patient.getId())) {
                if ("PENDING".equals(req.getStatus())) {
                    changeRequestRepository.delete(req);
                }
            }
        });
        
        return "redirect:/patient/appointments?deleteRequestSuccess=true";
    }
}
