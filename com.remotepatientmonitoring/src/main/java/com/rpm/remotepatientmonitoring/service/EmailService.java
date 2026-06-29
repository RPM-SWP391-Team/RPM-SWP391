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

    /**
     * Gửi email chứa mã OTP xác thực.
     */
    public void sendOtpEmail(String toEmail, String otp, String otpType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);

        String subject;
        String body;

        if ("REGISTRATION".equals(otpType)) {
            subject = "Xác thực email đăng ký - Hệ thống RPM";
            body = "Xin chào,\n\n" +
                    "Cảm ơn bạn đã đăng ký tài khoản trên Hệ thống Theo dõi Bệnh nhân Từ xa (RPM).\n\n" +
                    "Mã xác thực (OTP) của bạn là: " + otp + "\n\n" +
                    "Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n" +
                    "Hệ thống RPM";
        } else if ("PASSWORD_RESET".equals(otpType)) {
            subject = "Yêu cầu khôi phục mật khẩu - Hệ thống RPM";
            body = "Xin chào,\n\n" +
                    "Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản liên kết với email này trên Hệ thống Theo dõi Bệnh nhân Từ xa (RPM).\n\n" +
                    "Mã xác thực (OTP) để đặt lại mật khẩu của bạn là: " + otp + "\n\n" +
                    "Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n" +
                    "Hệ thống RPM";
        } else {
            subject = "Mã xác thực OTP - Hệ thống RPM";
            body = "Xin chào,\n\n" +
                    "Mã xác thực (OTP) của bạn là: " + otp + "\n\n" +
                    "Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
                    "Trân trọng,\n" +
                    "Hệ thống RPM";
        }

        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

}