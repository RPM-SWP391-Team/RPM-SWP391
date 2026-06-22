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

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

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

    @GetMapping("/appointments")
    public String getAppointments(Model model) throws JsonProcessingException {
        Patient patient = patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No patient found in database."));

        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patient.getId());
        List<ChangeRequest> changeRequests = changeRequestRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());

        LocalDate startDate = LocalDate.now().minusDays(7);
        List<DailyHealthLog> logs = healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patient.getId(), startDate);

        List<String> dates = new ArrayList<>();
        List<Integer> systolicList = new ArrayList<>();
        List<Integer> diastolicList = new ArrayList<>();
        List<Double> glucoseList = new ArrayList<>();

        for (DailyHealthLog log : logs) {
            dates.add(log.getLogDate().toString() + " (" + log.getLogType() + ")");
            systolicList.add(log.getSystolicBp() != null ? log.getSystolicBp() : 0);
            diastolicList.add(log.getDiastolicBp() != null ? log.getDiastolicBp() : 0);
            glucoseList.add(log.getGlucoseLevel() != null ? log.getGlucoseLevel().doubleValue() : 0.0);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("changeRequests", changeRequests);
        model.addAttribute("datesJson", objectMapper.writeValueAsString(dates));
        model.addAttribute("systolicJson", objectMapper.writeValueAsString(systolicList));
        model.addAttribute("diastolicJson", objectMapper.writeValueAsString(diastolicList));
        model.addAttribute("glucoseJson", objectMapper.writeValueAsString(glucoseList));

        return "patient/appointments";
    }

    @GetMapping("/request-change")
    public String requestChangePage(Model model) {
        Patient patient = patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No patient found in database."));

        ChangeRequest changeRequest = ChangeRequest.builder()
                .status("PENDING")
                .build();

        model.addAttribute("patient", patient);
        model.addAttribute("changeRequest", changeRequest);
        return "patient/request-change";
    }

    @PostMapping("/request-change")
    public String submitChangeRequest(@ModelAttribute("changeRequest") ChangeRequest changeRequest) {
        Patient patient = patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No patient found in database."));

        Doctor doctor = patient.getDoctor();
        if (doctor == null) {
            doctor = doctorRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No doctor found in database to receive requests."));
        }

        changeRequest.setPatient(patient);
        changeRequest.setDoctor(doctor);
        changeRequest.setStatus("PENDING");
        changeRequest.setCreatedAt(LocalDateTime.now());
        changeRequest.setUpdatedAt(LocalDateTime.now());

        changeRequestRepository.save(changeRequest);
        return "redirect:/patient/appointments?requestSuccess=true";
    }
}
