package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Models.SharedFile;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.FileRepo;
import com.example.swaraj.SkyLock.Repo.SharedFileRepo;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SharedFileService {

    private UsersRepo usersRepo;
    private FileRepo fileRepo;
    private SharedFileRepo sharedFileRepo;

    public SharedFileService(UsersRepo usersRepo, FileRepo fileRepo, SharedFileRepo sharedFileRepo) {
        this.usersRepo = usersRepo;
        this.fileRepo = fileRepo;
        this.sharedFileRepo = sharedFileRepo;
    }

    public Users getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new RuntimeException("User not authenticated");
        }
        return usersRepo.findByUsername(auth.getName());
    }

    @Transactional
    public String giveAccess(String fileId,String usernameOrEmail){
        Users user = getCurrentUser();
        // usernameOrEmail = other than owner
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
            sharedFile.setSharedAt(LocalDateTime.now());

        sharedFileRepo.save(sharedFile);
        return "File is shared to user";
    }
}
