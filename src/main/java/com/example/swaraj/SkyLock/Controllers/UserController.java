package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserService service;

    @PostMapping("/register")
    public Users registerUser(@RequestBody Users user){
        return service.registers(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody Users user){
        System.out.println(user.toString());
//        System.out.println("Sucess");
        return service.verify(user);
    }
}
