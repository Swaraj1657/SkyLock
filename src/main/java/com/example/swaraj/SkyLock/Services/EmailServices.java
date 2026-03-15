package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.Users;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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

    private String loadTemplate(String path) throws IOException, IOException {
        InputStream inputStream = getClass().getResourceAsStream(path);
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public void sendWelcomeMail(Users user) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("SkyLock<admin@skylock.dpdns.org>");
        helper.setTo(user.getEmail());
        helper.setSubject("Welcome to SkyLock");
        String body = loadTemplate("/templates/emails/welcome.html");
        body = body.replace("{{username}}", user.getUsername());
        helper.setText(body, true);
        mailSender.send(message);
    }

    public void sendVerificationMail(String username) throws MessagingException, IOException {
        Users user = userService.findByUsername(username);
        String token = jwtServices.genrateValidationToken(username);
        String verificationLink =
                "http://skylock.dpdns.org/verify-email?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =  new MimeMessageHelper(message, true , "UTF-8");
        helper.setFrom("SkyLockEmailVerify<emailverify@skylock.dpdns.org>");
        helper.setTo(user.getEmail());
        helper.setSubject("Verify your email - SkyLock");

        String body = loadTemplate("/templates/emails/verification.html");
        body = body.replace("{{username}}", user.getUsername());
        body = body.replace("{{verificationLink}}", verificationLink);
        helper.setText(body,true);
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

    public void resetPasswordEmail(Users user) throws MessagingException, IOException {
        String token = jwtServices.genrateValidationToken(user.getUsername());
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userService.save(user);
        String resetPassLink =
                "http://skylock.dpdns.org/resetPassword?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =  new MimeMessageHelper(message, true , "UTF-8");
        helper.setFrom("SkyLockResetPassword<resetpassword@skylock.dpdns.org>");
        helper.setTo(user.getEmail());
        helper.setSubject("Change Your Password - SkyLock");

        String body = loadTemplate("/templates/emails/resetpassword.html");

        body = body.replace("{{username}}", user.getUsername());
        body = body.replace("{{resetPassLink}}", resetPassLink);

        helper.setText(body, true);

        mailSender.send(message);
    }


    public void sharedFileEmail(String ownerName,String ownerEmail, String descrption ,String sharedEmail, String fileName, String fileLink) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =  new MimeMessageHelper(message, true , "UTF-8");
        helper.setFrom("SkyLockSharedFiles<sharedfiles@skylock.dpdns.org>");
        helper.setTo(sharedEmail);
        helper.setSubject(ownerName + " shared a file with you - SkyLock");
        String body = loadTemplate("/templates/emails/sharedfileemail.html");
        body = body.replace("{{ownerName}}", ownerName);
        body = body.replace("{{ownerEmail}}", ownerEmail);
        body = body.replace("{{fileName}}", fileName);
        body = body.replace("{{fileLink}}", fileLink);
        body = body.replace("{{message}}", descrption);
        helper.setText(body, true);

        mailSender.send(message);
    }
}