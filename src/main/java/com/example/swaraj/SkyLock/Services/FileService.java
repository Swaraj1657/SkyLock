package com.example.swaraj.SkyLock.Services;


import ch.qos.logback.core.util.StringUtil;
import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Models.Folder;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.FileRepo;
import com.example.swaraj.SkyLock.Repo.FolderRepo;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class FileService {

    private UsersRepo usersRepo;
    private FileRepo fileRepo;
    private FolderRepo folderRepo;

    public FileService(UsersRepo usersRepo, FileRepo fileRepo, FolderRepo folderRepo) {
        this.usersRepo = usersRepo;
        this.fileRepo = fileRepo;
        this.folderRepo = folderRepo;
    }

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usersRepo.findByUsername(auth.getName());
    }

    @Autowired
    FolderServices folderServices;

    @Value("${file.storage.path}")
    private String storagePath;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    public void createUserStroageFolder(String userId) {

        String basePath = storagePath;
        System.out.println(storagePath);
        String userFolder = storagePath + "user_" + userId;
        System.out.println(userFolder);
        File directory = new File(userFolder);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("directory is created");
        }
    }


    @Transactional
    public String deleteFile(String fileId){
        Users user = getCurrentUser();
        Optional<FileEntity> opt = fileRepo.findById(fileId);
        if(opt.isEmpty()){
            throw new RuntimeException("File is not found");
        }
        FileEntity file = opt.get();
        if(!file.getOwner().getId().equals(user.getId())){
            throw new RuntimeException("File is not belong to user");
        }
        try {
            Path filePath = Path.of(file.getPath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        user.setUsedStorage(user.getUsedStorage()-file.getSize());
        usersRepo.save(user);

        fileRepo.delete(file);
        return ("File removed Sucessfully");
    }
}
