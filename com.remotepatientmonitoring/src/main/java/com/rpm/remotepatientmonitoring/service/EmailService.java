package com.rpm.remotepatientmonitoring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public boolean sendDoctorPassword(String toEmail, String doctorName, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Tài khoản bác sĩ - Hệ thống RPM");
            message.setText(
                    "Xin chào Bác sĩ " + doctorName + ",\n\n" +
                            "Tài khoản của bạn đã được tạo trên Hệ thống Theo dõi Bệnh nhân Từ xa (RPM).\n\n" +
                            "Thông tin đăng nhập:\n" +
                            "Email: " + toEmail + "\n" +
                            "Mật khẩu: " + password + "\n\n" +
                            "Vui lòng đổi mật khẩu ngay sau lần đăng nhập đầu tiên.\n\n" +
                            "Trân trọng,\n" +
                            "Hệ thống RPM"
            );
            mailSender.send(message);
            return true;
        }catch(MailException e){
            return  false;
        }
    }
}