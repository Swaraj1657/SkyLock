package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UsersRepo repo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTServices jwtServices;
    private final EmailServices emailServices;
    @Autowired
    private FileService fileService;

    public UserService(
            UsersRepo repo,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JWTServices jwtServices,
            @Lazy EmailServices emailServices) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtServices = jwtServices;
        this.emailServices = emailServices;
    }

    public Users findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public Users findById(String id) {
        return repo.findById(id);
    }

    public Users findByUsernameOrEmail(String username, String email) {
        return repo.findByUsernameOrEmail(username, email);
    }

    public void save(Users users) {
        repo.save(users);
    }

    public Users registers(Users user) throws MessagingException, IOException {
        if (repo.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(user);
        fileService.createUserStroageFolder(user.getId());
        emailServices.sendWelcomeMail(user);
        return user;
    }

    public String verify(Users user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            return jwtServices.genrateToken(user.getUsername());
        } catch (BadCredentialsException exception) {
            throw new AuthenticationServiceException("Invalid username or password", exception);
        }
    }

    public boolean isEmailIsVerified(Users user) {

        Users dbUser = repo.findByUsername(user.getUsername());

        if (dbUser == null) {
            return false;
        }

        return dbUser.isEmailVerified();
    }

    public String verifyEmail(String token) {

        if (jwtServices.isTokenExpired(token)) {
            return "redirect:/validateEmail";
        }

        String username = jwtServices.extractUserName(token);
        Users user = repo.findByUsername(username);
        user.setEmailVerified(true);

        repo.save(user);
        return "Email Verified";
    }

    public String forgotPassword(String username) throws MessagingException, IOException {

        Users user = repo.findByUsernameOrEmail(username, username);

        if (user == null) {
            return "redirect:/forgotPassword?error";
        }
        emailServices.resetPasswordEmail(user);

        return "redirect:/forgotPassword?success";
    }

    public boolean resetPassword(String token, String password, String confirmPassword) {
        if (jwtServices.isTokenExpired(token)) {
            return false;
        }
        if (!password.equals(confirmPassword)) {
            return false;
        }
        String username = jwtServices.extractUserName(token);
        Users user = findByUsername(username);
        if(!token.equals(user.getResetToken())){
            return false;
        }
        if(user.getResetTokenExpiry().isBefore(LocalDateTime.now())){
            return false;
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        repo.save(user);
        return true;
    }

}
