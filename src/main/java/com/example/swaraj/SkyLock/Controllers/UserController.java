package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.DTO.AuthResponse;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Users registerUser(@RequestBody Users user) {
        return service.registers(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody Users user) {
        String token = service.verify(user);
        return new AuthResponse(token, "Bearer");
    }
}
