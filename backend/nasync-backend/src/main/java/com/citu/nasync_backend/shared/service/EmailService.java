package com.citu.nasync_backend.shared.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendWelcomeEmail(String personalEmail,
        String schoolEmail, String fullName, String schoolId,
            String defaultPassword, String department, String branch) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(personalEmail, schoolEmail);
        msg.setSubject("Welcome to NASync - Your Account Credentials");

        msg.setText(String.format(
                "Dear %s,\n\n" +

                        "--------------------------------------------------\n" +
                        "               WELCOME TO NASYNC\n" +
                        "--------------------------------------------------\n\n" +

                        "Your account has been successfully created.\n" +
                        "Please find your login credentials below:\n\n" +

                        "--------------------------------------------------\n" +
                        "ACCOUNT INFORMATION\n" +
                        "--------------------------------------------------\n" +
                        "School ID          : %s\n" +
                        "Temporary Password : %s\n" +
                        "Department         : %s\n" +
                        "Branch             : %s\n" +
                        "--------------------------------------------------\n\n" +

                        "IMPORTANT:\n" +
                        "For security purposes, please log in and change\n" +
                        "your password immediately after your first login.\n\n" +

                        "If you encounter any issues accessing your account,\n" +
                        "please contact the system administrator.\n\n" +

                        "--------------------------------------------------\n" +
                        "Best regards,\n" +
                        "OAS Administration\n" +
                        "NASync System\n" +
                        "--------------------------------------------------",

                fullName,
                schoolId,
                defaultPassword,
                department,
                branch));

        mailSender.send(msg);
    }

    public void sendDutyApprovedEmail(String personalGmail, String schoolEmail, String fullName,
            String dutyDate, String dutyType, String remarks) {
        if (personalGmail == null || personalGmail.isBlank()) {
            log.warn("sendDutyApprovedEmail skipped — personalGmail is null for {}", fullName);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(personalGmail, schoolEmail);
            msg.setSubject("NASync — Your Duty Has Been Approved");
            msg.setText(String.format(
                "Dear %s,\n\n" +
                "--------------------------------------------------\n" +
                "            DUTY APPROVED\n" +
                "--------------------------------------------------\n\n" +
                "Your duty record has been approved.\n\n" +
                "--------------------------------------------------\n" +
                "DUTY DETAILS\n" +
                "--------------------------------------------------\n" +
                "Date       : %s\n" +
                "Type       : %s\n" +
                "Remarks    : %s\n" +
                "--------------------------------------------------\n\n" +
                "If you have questions, contact your Department Head.\n\n" +
                "--------------------------------------------------\n" +
                "Best regards,\n" +
                "OAS Administration\n" +
                "NASync System\n" +
                "--------------------------------------------------",
                fullName, dutyDate, dutyType,
                remarks != null && !remarks.isBlank() ? remarks : "None"));
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send duty approval email to {}: {}", personalGmail, e.getMessage());
        }
    }

    public void sendDutyReminderEmail(
            String schoolEmail,
            String personalGmail,
            String fullName,
            double totalRequired,
            double completedHours,
            double remainingHours,
            long totalLates,
            double missedHours) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            if (personalGmail != null && !personalGmail.isBlank()) {
                msg.setTo(schoolEmail, personalGmail);
            } else {
                msg.setTo(schoolEmail);
            }
            msg.setSubject("NASync — Duty Obligation Reminder");
            msg.setText(String.format(
                "Dear %s,\n\n" +
                "--------------------------------------------------\n" +
                "         DUTY OBLIGATION REMINDER\n" +
                "--------------------------------------------------\n\n" +
                "This is a reminder from the Office of Admissions\n" +
                "and Scholarships (OAS) regarding your NAS duty\n" +
                "obligations for the current semester.\n\n" +
                "--------------------------------------------------\n" +
                "YOUR DUTY STATUS SUMMARY\n" +
                "--------------------------------------------------\n" +
                "Total Required Hours : %.1f hrs\n" +
                "Completed Hours      : %.1f hrs\n" +
                "Remaining Hours      : %.1f hrs\n" +
                "Missed Hours         : %.1f hrs\n" +
                "Total Lates          : %d\n" +
                "--------------------------------------------------\n\n" +
                "You are encouraged to complete your remaining duty\n" +
                "hours as soon as possible. Failure to complete your\n" +
                "duty obligations may affect your scholarship standing.\n\n" +
                "If you have questions, please reach out to the OAS\n" +
                "or your assigned Department Head.\n\n" +
                "--------------------------------------------------\n" +
                "Best regards,\n" +
                "OAS Administration\n" +
                "NASync System\n" +
                "--------------------------------------------------",
                fullName, totalRequired, completedHours,
                remainingHours, missedHours, totalLates));
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send duty reminder email to {}: {}", schoolEmail, e.getMessage());
            throw new RuntimeException("Failed to send reminder email: " + e.getMessage());
        }
    }

    public void sendDutyRejectedEmail(String personalGmail, String schoolEmail, String fullName,
            String dutyDate, String dutyType, String remarks) {
        if (personalGmail == null || personalGmail.isBlank()) {
            log.warn("sendDutyRejectedEmail skipped — personalGmail is null for {}", fullName);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(personalGmail, schoolEmail);
            msg.setSubject("NASync — Your Duty Has Been Rejected");
            msg.setText(String.format(
                "Dear %s,\n\n" +
                "--------------------------------------------------\n" +
                "            DUTY REJECTED\n" +
                "--------------------------------------------------\n\n" +
                "Unfortunately, your duty record has been rejected.\n\n" +
                "--------------------------------------------------\n" +
                "DUTY DETAILS\n" +
                "--------------------------------------------------\n" +
                "Date       : %s\n" +
                "Type       : %s\n" +
                "Remarks    : %s\n" +
                "--------------------------------------------------\n\n" +
                "Please review the remarks above and re-submit if applicable,\n" +
                "or contact your Department Head for clarification.\n\n" +
                "--------------------------------------------------\n" +
                "Best regards,\n" +
                "OAS Administration\n" +
                "NASync System\n" +
                "--------------------------------------------------\n",
                fullName, dutyDate, dutyType,
                remarks != null && !remarks.isBlank() ? remarks : "Rejected by Department Head"));
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send duty rejection email to {}: {}", personalGmail, e.getMessage());
        }
    }
}
