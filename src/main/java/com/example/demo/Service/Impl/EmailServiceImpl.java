package com.example.demo.Service.Impl;
import com.example.demo.Service.LoggerService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

import com.example.demo.Service.EmailService;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private LoggerService loggerService;
    @Override
    public void sendActivationEmail(String to, String subject, String activationLink) throws MessagingException {
        loggerService.logInfo("Bắt đầu gửi email  tới: " + to);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(
                "<h2>Xác minh tài khoản</h2>" +
                        "<p>Vui lòng nhấp vào liên kết dưới đây để kích hoạt tài khoản của bạn:</p>" +
                        "<p><a href='" + activationLink + "'>Kích hoạt tài khoản</a></p>" +
                        "<p>Liên kết này sẽ hết hạn sau 24 giờ.</p>" +
                        "<p>Trân trọng,<br>RED WINGS</p>",
                true
        );

        mailSender.send(message);
    }

    @Override
    public void sendNewPasswordEmail(String to, String subject, String newPassword) throws MessagingException {
        loggerService.logInfo("Bắt đầu gửi email  tới: " + to);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(
                "<h2>Mật khẩu mới</h2>" +
                        "<p>Mật khẩu mới của bạn là:</p>" +
                        "<p><strong>" + newPassword + "</strong></p>" +
                        "<p>Vui lòng đăng nhập và đổi mật khẩu để bảo mật tài khoản.</p>" +
                        "<p>Trân trọng,<br>RED WINGS</p>",
                true
        );

        mailSender.send(message);
    }
}