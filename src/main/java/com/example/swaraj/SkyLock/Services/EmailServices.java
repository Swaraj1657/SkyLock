package com.example.swaraj.SkyLock.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServices {

    private final JavaMailSender mailSender;

    public EmailServices(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String to, String subject, String body)
            throws MessagingException {

        System.out.println("MAIL SENDING STARTED → " + to);

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        // ✅ VERY IMPORTANT (Gmail Trust)
        helper.setFrom("SkyLock <admin@skylock.dpdns.org>");

        // ✅ Alignment fix
        helper.setReplyTo("admin@skylock.dpdns.org");

        helper.setTo(to);
        helper.setSubject(subject);

        // ✅ Send BOTH HTML + TEXT fallback
        helper.setText(
                "SkyLock Notification",   // plain text
                body                      // html
        );

        mailSender.send(message);

        System.out.println("MAIL SENT SUCCESSFULLY ✅");
    }
}