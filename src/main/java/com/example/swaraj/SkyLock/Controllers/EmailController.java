package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.Services.EmailServices;
import com.example.swaraj.SkyLock.Services.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class EmailController {

    private EmailServices emailServices;
    private UserService userService;
    public EmailController(EmailServices emailServices, UserService userService){
        this.emailServices = emailServices;
        this.userService = userService;
    }


    @PostMapping("/send-verification")
    public String sendVerification(HttpServletRequest request) throws MessagingException, IOException {

        String username = (String) request.getSession().getAttribute("User");

        emailServices.sendVerificationMail(username);

        return "redirect:/validateEmail";
    }


    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam String token){
        userService.verifyEmail(token);
        return "redirect:/loginPage?verified";
    }
}
