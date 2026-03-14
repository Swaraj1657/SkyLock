package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.Services.EmailServices;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmailController {

    private EmailServices emailServices;
    public EmailController(EmailServices emailServices){
        this.emailServices = emailServices;
    }


    @PostMapping("/send-verification")
    public String sendVerification(HttpServletRequest request) throws MessagingException {

        String username = (String) request.getSession().getAttribute("User");

        emailServices.sendVerificationMail(username);

        return "redirect:/validateEmail";
    }


    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam String token){
        emailServices.verifyEmail(token);
        return "redirect:/loginPage?verified";
    }
}
