
package com.studentregistration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAccountCreatedEmail(String studentEmail,
                                        String studentName) {

        String subject = "Student Registration Successful";

        String message = "Hello " + studentName + ",\n\n"
                + "Your student account has been created successfully.\n"
                + "You can now log in and register for courses.\n\n"
                + "Regards,\nStudent Registration System";

        sendEmail(studentEmail, subject, message);
    }

    public void sendLoginNotification(String studentEmail,
                                      String studentName) {

        String subject = "Successful Login Notification";

        String message = "Hello " + studentName + ",\n\n"
                + "You have successfully logged in to the "
                + "Student Registration System.\n\n"
                + "If this wasn't you, please contact the administrator.\n\n"
                + "Regards,\nStudent Registration System";

        sendEmail(studentEmail, subject, message);
    }

    private void sendEmail(String recipient,
                           String subject,
                           String message) {

        if (senderEmail == null || senderEmail.isBlank()) {
            logger.warn("Email not sent: sender email is not configured.");
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();

            mail.setFrom(senderEmail);
            mail.setTo(recipient);
            mail.setSubject(subject);
            mail.setText(message);

            mailSender.send(mail);

            logger.info("Email sent successfully to {}", recipient);

        } catch (MailException exception) {
            logger.error("Failed to send email to {}", recipient, exception);
        }
    }
    
    public void sendEnrollmentConfirmation(
            String studentEmail,
            String studentName,
            String courseTitle,
            String courseCode) {

        String subject = "Course Enrollment Confirmation";

        String message = "Hello " + studentName + ",\n\n"
                + "Your enrollment was successful.\n\n"
                + "Course: " + courseTitle + "\n"
                + "Course Code: " + courseCode + "\n\n"
                + "Thank you for registering.\n"
                + "Student Registration System";

        sendEmail(studentEmail, subject, message);
    }
    

public void sendEnrollmentDeletionEmail(
        String studentEmail,
        String studentName,
        String courseTitle,
        String courseCode) {

    String subject = "Course Enrollment Removed";

    String message = "Hello " + studentName + ",\n\n"
            + "Your enrollment in the following course has been removed "
            + "by the administrator.\n\n"
            + "Course: " + courseTitle + "\n"
            + "Course Code: " + courseCode + "\n\n"
            + "If you have questions, please contact the administrator.\n\n"
            + "Student Registration System";

    sendEmail(studentEmail, subject, message);
}


public void sendAccountDeletionEmail(
        String studentEmail,
        String studentName) {

    String subject = "Student Account Deleted";

    String message = "Hello " + studentName + ",\n\n"
            + "Your Student Course Registration System account "
            + "has been deleted by the administrator.\n\n"
            + "Your account and course enrollments are no longer active.\n\n"
            + "If you believe this was a mistake, please contact "
            + "the administrator.\n\n"
            + "Student Registration System";

    sendEmail(studentEmail, subject, message);
}


}
