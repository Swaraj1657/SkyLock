package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Models.SharedFile;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.FileRepo;
import com.example.swaraj.SkyLock.Repo.SharedFileRepo;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import jakarta.mail.MessagingException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SharedFileService {

    private final EmailServices emailServices;
    private UsersRepo usersRepo;
    private FileRepo fileRepo;
    private SharedFileRepo sharedFileRepo;

    public SharedFileService(UsersRepo usersRepo, FileRepo fileRepo, SharedFileRepo sharedFileRepo, EmailServices emailServices) {
        this.usersRepo = usersRepo;
        this.fileRepo = fileRepo;
        this.sharedFileRepo = sharedFileRepo;
        this.emailServices = emailServices;
    }

    public Users getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new RuntimeException("User not authenticated");
        }
        return usersRepo.findByUsername(auth.getName());
    }

    @Transactional
    public String giveAccess(String fileId,String usernameOrEmail, String role) throws MessagingException, IOException {
        Users user = getCurrentUser();
        String descrption = "File is sended with you";
        Users sharedUser = usersRepo.findByUsernameOrEmail(usernameOrEmail,usernameOrEmail);

        if (sharedUser == null)
            throw new RuntimeException("User is not found");

        if(user.getId().equals(sharedUser.getId())){
            throw new RuntimeException("You already own this file");
        }

        Optional<FileEntity> opt = fileRepo.findById(fileId);
        if(opt.isEmpty()){
            throw new RuntimeException("File is not found");
        }
        FileEntity file =  opt.get();
        if(!file.getOwner().getId().equals(user.getId())){
            throw new RuntimeException("user is not Authorized");
        }

        if(sharedFileRepo.existsByFileAndSharedwith(file,sharedUser))
            throw new RuntimeException("File is already Shared");

        SharedFile sharedFile = new SharedFile();
        sharedFile.setFile(file);
        sharedFile.setSharedwith(sharedUser);
        sharedFile.setRole(role != null ? role : "viewer");
        sharedFile.setSharedAt(LocalDateTime.now());

        sharedFileRepo.save(sharedFile);
        
        String fileLink = "http://skylock.dpdns.org/home?view=shared&fileId=" + file.getId();

        emailServices.sharedFileEmail(user.getUsername(), user.getEmail(), descrption, sharedUser.getEmail(), file.getFilename(), fileLink);

        return "File is shared to user";
    }

    public List<Map<String, Object>> getSharedWithMe() {
        Users user = getCurrentUser();

        List<SharedFile> sharedFilesList = sharedFileRepo.findBySharedwith(user);

        List<Map<String, Object>> fileList = new ArrayList<>();
        for (SharedFile sf : sharedFilesList) {
            Map<String, Object> fm = new HashMap<>();
            fm.put("id", sf.getFile().getId()); 
            fm.put("filename", sf.getFile().getFilename());
            fm.put("size", sf.getFile().getSize());
            fm.put("uploadedAt", sf.getFile().getUploadedAt() != null ? sf.getFile().getUploadedAt().toString() : ""); 
            fm.put("ownerName", sf.getFile().getOwner().getUsername());
            fm.put("role", sf.getRole());
            fm.put("sharedAt", sf.getSharedAt() != null ? sf.getSharedAt().toString() : "");
            fileList.add(fm);
        }

        return fileList;
    }
}
