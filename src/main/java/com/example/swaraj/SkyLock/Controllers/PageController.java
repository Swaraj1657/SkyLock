package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.Services.EmailServices;
import com.example.swaraj.SkyLock.Services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private EmailServices emailServices;

//    public PageController(EmailServices emailServices) {
//        this.emailServices = emailServices;
//    }

    @GetMapping("/")
    public String indexPage(Authentication authentication){
        if(authentication != null && authentication.isAuthenticated()){
            return "redirect:/home";
        }
        return "login";
    }


    @GetMapping("/registerPage")
    public String registerPage() {
        return "register";   // returns register.html
    }

    @GetMapping("/loginPage")
    public String loginPage(Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            return "redirect:/home";
        }
        return "login";      // returns login.html
    }

    @GetMapping("/uploadPage")
    public String uploadPage(){
        return "upload";
    }


    @GetMapping("/validateEmail")
    public String validateEmailPage(){
        return "validateEmail";
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {

        emailServices.verifyEmail(token);

        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully"
        ));
    }

    @GetMapping("/forgotPassword")
    public String forgotPassword(){
        return "forgot-password";
    }

    @GetMapping("/resetPassword")
    public String showResetPage(@RequestParam String token, Model model) {

        model.addAttribute("token", token);

        return "resetPassword";
    }
    }

