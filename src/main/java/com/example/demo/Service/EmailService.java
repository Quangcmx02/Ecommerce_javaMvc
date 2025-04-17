package com.example.demo.Service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendActivationEmail(String to, String subject, String activationLink) throws MessagingException;
    void sendNewPasswordEmail(String to, String subject, String newPassword) throws MessagingException;

}
