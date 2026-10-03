package com.campus.helpdesk.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName, String rawPassword, String role) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        message.setFrom("sliitcampushelpdesk@gmail.com"); 
        message.setTo(toEmail);
        message.setSubject("Welcome to SLIIT Help Desk - Your Account Details");
        
        String body = "Hello " + fullName + ",\n\n"
                    + "Your account has been successfully created in the SLIIT Help Desk System as a " + role + ".\n\n"
                    + "Here are your login credentials:\n"
                    + "Login Email: " + toEmail + "\n"
                    + "Temporary Password: " + rawPassword + "\n\n"
                    + "For your security, please log in and change your password immediately.\n\n"
                    + "Best Regards,\n"
                    + "SLIIT Campus Help Desk Team";

        message.setText(body);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to " + toEmail + ": " + e.getMessage());
        }
    }
}
