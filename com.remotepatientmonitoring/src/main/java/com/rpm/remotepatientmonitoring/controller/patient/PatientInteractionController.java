package com.rpm.remotepatientmonitoring.controller.patient;

import com.rpm.remotepatientmonitoring.model.Appointment;
import com.rpm.remotepatientmonitoring.model.ChangeRequest;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AppointmentRepository;
import com.rpm.remotepatientmonitoring.repository.ChangeRequestRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

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

    @GetMapping("/appointments")
    public String getAppointments(Model model) {
        Patient patient = patientRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No patient found in database."));

        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patient.getId());
        List<ChangeRequest> changeRequests = changeRequestRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId());

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        model.addAttribute("changeRequests", changeRequests);
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
