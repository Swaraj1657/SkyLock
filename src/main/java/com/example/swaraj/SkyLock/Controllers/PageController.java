package com.example.swaraj.SkyLock.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

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

    @GetMapping("/home")
    public String home(){
        return "home";
    }
}
