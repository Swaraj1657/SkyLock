package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Models.Folder;
import com.example.swaraj.SkyLock.Models.SharedFolder;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SharedFolderServices {

    private UsersRepo usersRepo;
    private FolderRepo folderRepo;
    private SharedFileRepo sharedFileRepo;
    private SharedFolderRepo sharedFolderRepo;
    private final EmailServices emailServices;

    public SharedFolderServices(UsersRepo usersRepo, FolderRepo folderRepo, SharedFileRepo sharedFileRepo, SharedFolderRepo sharedFolderRepo, EmailServices emailServices) {
        this.usersRepo = usersRepo;
        this.folderRepo = folderRepo;
        this.sharedFileRepo = sharedFileRepo;
        this.sharedFolderRepo = sharedFolderRepo;
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
    public void giveAccess(String id, String  usernameOrEmail, String role) throws jakarta.mail.MessagingException, java.io.IOException {
        Users user = getCurrentUser();
        Users sharedUser = usersRepo.findByUsernameOrEmail(usernameOrEmail,usernameOrEmail);
        if(sharedUser == null){
            throw new RuntimeException("User is not found");
        }
        if(user.getId().equals(sharedUser.getId())){
            throw new RuntimeException("You owned this file");
        }

        Folder folder = folderRepo.findByIdIs(id);
        if(folder == null){
            throw new RuntimeException("Folder is not found");
        }
        if(!folder.getOwner().getId().equals(user.getId())){
            throw new RuntimeException("Unauthrized User");
        }
        if(sharedFolderRepo.existsByFolderAndSharedWith(folder,sharedUser)){
            throw new RuntimeException("File is already Shared");
        }

        SharedFolder sharedFolder = new SharedFolder();
        sharedFolder.setFolder(folder);
        sharedFolder.setSharedWith(sharedUser);
        sharedFolder.setSharedAt(LocalDateTime.now());
        sharedFolder.setRole(role != null ? role : "viewer");

        sharedFolderRepo.save(sharedFolder);

        String descrption = "Folder is shared with you";
        String folderLink = "http://skylock.dpdns.org/home?view=shared&folderId=" + folder.getId();
//        emailServices.sharedFolderEmail(user.getUsername(), user.getEmail(), descrption, sharedUser.getEmail(), folder.getName(), folderLink);
    }

    public List<Map<String, Object>> getSharedWithMe() {
        Users user = getCurrentUser();
        List<SharedFolder> sharedFoldersList = sharedFolderRepo.findBySharedWith(user);

        List<Map<String, Object>> folderList = new ArrayList<>();
        for (SharedFolder sf : sharedFoldersList) {
            Map<String, Object> fm = new HashMap<>();
            fm.put("id", sf.getFolder().getId()); 
            fm.put("name", sf.getFolder().getName()); 
            
            Folder f = sf.getFolder();
            int fileCount = f.getFiles() != null ? f.getFiles().size() : 0;
            int subFolderCount = f.getSubFolders() != null ? f.getSubFolders().size() : 0;
            fm.put("itemCount", fileCount + subFolderCount); 
            
            fm.put("ownerName", f.getOwner().getUsername());
            fm.put("role", sf.getRole());
            fm.put("sharedAt", sf.getSharedAt() != null ? sf.getSharedAt().toString() : "");
            folderList.add(fm);
        }

        return folderList;
    }

    public List<Map<String, Object>> getSharesForFolder(String folderId) {
        Users user = getCurrentUser();
        Folder folder = folderRepo.findByIdIs(folderId);
        if (folder == null) throw new RuntimeException("Folder is not found");
        if (!folder.getOwner().getId().equals(user.getId())) throw new RuntimeException("user is not Authorized");

        List<SharedFolder> sharedFolders = sharedFolderRepo.findByFolder(folder);
        List<Map<String, Object>> shares = new ArrayList<>();
        for (SharedFolder sf : sharedFolders) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sf.getId());
            map.put("email", sf.getSharedWith().getEmail());
            map.put("name", sf.getSharedWith().getUsername());
            map.put("role", sf.getRole());
            shares.add(map);
        }
        return shares;
    }

    @Transactional
    public void updateShareRole(String shareId, String newRole) {
        Users user = getCurrentUser();
        java.util.Optional<SharedFolder> opt = sharedFolderRepo.findById(shareId);
        if (opt.isEmpty()) throw new RuntimeException("Share not found");
        SharedFolder sharedFolder = opt.get();
        if (!sharedFolder.getFolder().getOwner().getId().equals(user.getId())) throw new RuntimeException("user is not Authorized");
        
        sharedFolder.setRole(newRole);
        sharedFolderRepo.save(sharedFolder);
    }

    @Transactional
    public void removeShare(String shareId) {
        Users user = getCurrentUser();
        java.util.Optional<SharedFolder> opt = sharedFolderRepo.findById(shareId);
        if (opt.isEmpty()) throw new RuntimeException("Share not found");
        SharedFolder sharedFolder = opt.get();
        if (!sharedFolder.getFolder().getOwner().getId().equals(user.getId())) throw new RuntimeException("user is not Authorized");
        
        sharedFolderRepo.delete(sharedFolder);
    }

    @Transactional
    public void updateGeneralAccess(String folderId, String access) {
        Users user = getCurrentUser();
        Folder folder = folderRepo.findByIdIs(folderId);
        if (folder == null) throw new RuntimeException("Folder is not found");
        if (!folder.getOwner().getId().equals(user.getId())) throw new RuntimeException("user is not Authorized");
        
        folder.setGeneralAccess(access);
        folderRepo.save(folder);
    }
}
