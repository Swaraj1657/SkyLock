package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.Users;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServices {

    private final JavaMailSender mailSender;
    private final JWTServices jwtServices;
    private final UserService userService;

    public EmailServices(JavaMailSender mailSender,
                         JWTServices jwtServices,
                         UserService userService) {
        this.mailSender = mailSender;
        this.jwtServices = jwtServices;
        this.userService = userService;
    }

//    public void sendMail(String to, String subject, String body)
//            throws MessagingException {
//
//        System.out.println("MAIL SENDING STARTED → " + to);
//
//        MimeMessage message = mailSender.createMimeMessage();
//
//        MimeMessageHelper helper =
//                new MimeMessageHelper(message, true, "UTF-8");
//
//        // ✅ VERY IMPORTANT (Gmail Trust)
//        helper.setFrom("SkyLock <admin@skylock.dpdns.org>");
//
//        // ✅ Alignment fix
//        helper.setReplyTo("admin@skylock.dpdns.org");
//
//        helper.setTo(to);
//        helper.setSubject(subject);
//
//        // ✅ Send BOTH HTML + TEXT fallback
//        helper.setText(
//                "SkyLock Notification",   // plain text
//                body                      // html
//        );
//
//        mailSender.send(message);
//
//        System.out.println("MAIL SENT SUCCESSFULLY ✅");
//    }

    public void sendVerificationMail(String username) throws MessagingException {
        Users user = userService.findByUsername(username);
        System.out.println(user.getEmail());
        String token = jwtServices.genrateValidationToken(username);
        String verificationLink =
                "http://skylock.dpdns.org/verify-email?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =  new MimeMessageHelper(message, true , "UTF-8");
        helper.setFrom("SkyLock<admin@skylock.dpdns.org>");
        helper.setTo(user.getEmail());
        helper.setSubject("Verify your email - SkyLock");

        String body = "Hello " + user.getUsername() + ",\n\n"
                + "Please verify your email by clicking the link below:\n\n"
                + verificationLink + "\n\n"
                + "This link will expire in 10 minutes.\n\n"
                + "SkyLock Security Team";

        helper.setText(body);

        mailSender.send(message);
    }

    public String verifyEmail(String token){

        if(jwtServices.isTokenExpired(token)){
            return "redirect:/validateEmail";
        }

        String username = jwtServices.extractUserName(token);
        Users user = userService.findByUsername(username);

        user.setEmailVerified(true);
        userService.save(user);

        return "Email Verified";
    }
}