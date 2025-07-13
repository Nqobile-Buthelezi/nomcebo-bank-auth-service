package za.co.nomcebo.bank.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import za.co.nomcebo.bank.auth.model.User;

@Service
public class NotificationService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to Nomcebo Bank! Please verify your email";
        String verificationLink = "https://your-app-url/verify?email=" + user.getEmail(); // Replace with actual link logic
        String text = String.format(
                "Dear %s,\n\nThank you for registering with Nomcebo Bank. Please verify your " +
                        "email by clicking the link below:\n%s\n\nIf you did not register, " +
                        "please ignore this email.\n\nBest regards,\nNomcebo Bank Team",
                user.getFirstName(), verificationLink
        );
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Nomcebo Bank Password Reset";
        String resetLink = "https://your-app-url/reset-password?token=" + resetToken; // Replace with actual link logic
        String text = String.format(
                "Dear %s,\n\nWe received a request to reset your password. " +
                        "Please reset your password by clicking the link below:\n%s\n\nIf you did not request a password reset, please ignore this email.\n\nBest regards,\nNomcebo Bank Team",
                user.getFirstName(), resetLink
        );
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
