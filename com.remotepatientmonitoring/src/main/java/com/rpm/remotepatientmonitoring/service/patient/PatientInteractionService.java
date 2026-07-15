package com.rpm.remotepatientmonitoring.service.patient;

import com.rpm.remotepatientmonitoring.model.*;
import com.rpm.remotepatientmonitoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PatientInteractionService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ChangeRequestRepository changeRequestRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Appointment> getAppointments(Integer patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patientId);
    }

    public List<ChangeRequest> getChangeRequests(Integer patientId) {
        return changeRequestRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public List<DailyHealthLog> getLatestLogsForChart(Integer patientId, LocalDate chartStartDate) {
        return healthLogRepository.findByPatientIdAndLogDateGreaterThanEqualOrderByLogDateAsc(patientId, chartStartDate);
    }

    public Page<DailyHealthLog> getBpLogsPage(Integer patientId, String filterRange, LocalDate filterDate, Pageable pageable) {
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

        if (filterDate != null) {
            return healthLogRepository.findByPatientIdAndSystolicBpIsNotNullAndLogDateOrderByLogTimeDesc(patientId, filterDate, pageable);
        } else if (rangeStart != null && rangeEnd != null) {
            return healthLogRepository.findByPatientIdAndSystolicBpIsNotNullAndLogDateBetweenOrderByLogTimeDesc(patientId, rangeStart, rangeEnd, pageable);
        } else {
            return healthLogRepository.findByPatientIdAndSystolicBpIsNotNullOrderByLogTimeDesc(patientId, pageable);
        }
    }

    public Page<DailyHealthLog> getGlucoseLogsPage(Integer patientId, String filterRange, LocalDate filterDate, Pageable pageable) {
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

        if (filterDate != null) {
            return healthLogRepository.findByPatientIdAndGlucoseLevelIsNotNullAndLogDateOrderByLogTimeDesc(patientId, filterDate, pageable);
        } else if (rangeStart != null && rangeEnd != null) {
            return healthLogRepository.findByPatientIdAndGlucoseLevelIsNotNullAndLogDateBetweenOrderByLogTimeDesc(patientId, rangeStart, rangeEnd, pageable);
        } else {
            return healthLogRepository.findByPatientIdAndGlucoseLevelIsNotNullOrderByLogTimeDesc(patientId, pageable);
        }
    }

    public List<Doctor> getAvailableDoctors(Integer hospitalId) {
        return doctorRepository.findAvailableDoctorsByHospital(hospitalId);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional
    public void createChangeRequest(ChangeRequest changeRequest, Patient patient) {
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

        if (doctor != null) {
            Notification notif = Notification.builder()
                    .doctor(doctor)
                    .recipientType("DOCTOR")
                    .title("Yêu cầu thay đổi mới")
                    .content("Bệnh nhân " + patient.getFullName() + " vừa gửi một yêu cầu " + 
                            (changeRequest.getRequestType().equals("RESCHEDULE") ? "đổi lịch khám" : "thay đổi phác đồ") + ".")
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
        }
    }

    @Transactional
    public void bookAppointment(Patient patient, Integer doctorId, String appointmentType, String patientRequestReason, LocalDateTime apptTime) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid doctor Id: " + doctorId);
        }
        Doctor doctor = doctorOpt.get();

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
    }

    @Transactional
    public void deleteAppointment(Integer id, Integer patientId) {
        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        if (apptOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid appointment id: " + id);
        }
        Appointment appt = apptOpt.get();
        if (!appt.getPatient().getId().equals(patientId)) {
            throw new IllegalStateException("Bạn không có quyền hủy lịch hẹn này!");
        }
        appointmentRepository.delete(appt);
    }

    @Transactional
    public void deleteChangeRequest(Integer id, Integer patientId) {
        Optional<ChangeRequest> reqOpt = changeRequestRepository.findById(id);
        if (reqOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid request id: " + id);
        }
        ChangeRequest req = reqOpt.get();
        if (!req.getPatient().getId().equals(patientId)) {
            throw new IllegalStateException("Bạn không có quyền xóa yêu cầu này!");
        }
        changeRequestRepository.delete(req);
    }
}
