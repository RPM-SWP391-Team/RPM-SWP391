package com.rpm.remotepatientmonitoring.scheduler;

import com.rpm.remotepatientmonitoring.model.Appointment;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Notification;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AppointmentRepository;
import com.rpm.remotepatientmonitoring.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderSchedulerTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private AppointmentReminderScheduler scheduler;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1);
        patient.setFullName("Nguyen Van A");

        doctor = new Doctor();
        doctor.setId(2);
        doctor.setFullName("Dr. Smith");

        appointment = Appointment.builder()
                .id(10)
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .status("ACCEPTED")
                .reminderSent2days(false)
                .location("Phong 102, Kế hoạch")
                .build();
    }

    @Test
    void sendAppointmentReminders_Success() {
        List<Appointment> list = new ArrayList<>();
        list.add(appointment);

        when(appointmentRepository.findByStatusAndReminderSent2daysFalseAndAppointmentTimeBetween(
                eq("ACCEPTED"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(list);

        scheduler.sendAppointmentReminders();

        // Kiểm tra xem Notification có được lưu không
        ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notifCaptor.capture());

        Notification savedNotif = notifCaptor.getValue();
        assertEquals("PATIENT", savedNotif.getRecipientType());
        assertEquals(1, savedNotif.getRecipientId());
        assertEquals("APPOINTMENT_REMINDER", savedNotif.getNotificationType());
        assertTrue(savedNotif.getContent().contains("Dr. Smith"));
        assertTrue(savedNotif.getContent().contains("Phong 102, Kế hoạch"));

        // Kiểm tra xem trạng thái của appointment có được cập nhật thành đã gửi nhắc nhở không
        ArgumentCaptor<Appointment> apptCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, times(1)).save(apptCaptor.capture());

        Appointment savedAppt = apptCaptor.getValue();
        assertTrue(savedAppt.getReminderSent2days());
    }

    @Test
    void sendAppointmentReminders_EmptyList() {
        when(appointmentRepository.findByStatusAndReminderSent2daysFalseAndAppointmentTimeBetween(
                eq("ACCEPTED"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        scheduler.sendAppointmentReminders();

        verify(notificationRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }
}
