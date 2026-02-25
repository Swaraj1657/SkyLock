package com.example.swaraj.SkyLock.Services;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class FileService {

    @Value("${file.storage.path}")
    private String storagePath;

    public void createUserStroageFolder(String userId){

        String basePath = storagePath;
        System.out.println(storagePath);
        String userFolder = storagePath + "/user_" + userId;
        System.out.println(userFolder);
        File directory = new File(userFolder);
        if(!directory.exists()){
            directory.mkdirs();
            System.out.println("directory is created");
        }
    }

    @Transactional
    public String uploadFile(MultipartFile file , Long folderId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        return "";
    }
}
