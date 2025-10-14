package com.example.health_care_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Movies App");

            String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
            String emailBody = buildEmailBody(resetUrl);

            message.setText(emailBody);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }

    private String buildEmailBody(String resetUrl) {
        return "Dear User,\n\n" +
                "We received a request to reset your password for your Movies App account.\n\n" +
                "Click the link below to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link will expire in 24 hours for security reasons.\n\n" +
                "If you didn't request this password reset, please ignore this email. " +
                "Your password will remain unchanged.\n\n" +
                "Best regards,\n" +
                "The Movies App Team";
    }
}