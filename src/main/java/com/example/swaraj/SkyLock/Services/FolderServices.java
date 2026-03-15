package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Models.Folder;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.FolderRepo;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class FolderServices {

    private final UsersRepo usersRepo;
    private final FolderRepo folderRepo;

    public FolderServices(FolderRepo folderRepo, UsersRepo usersRepo) {
        this.folderRepo = folderRepo;
        this.usersRepo = usersRepo;
    }

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usersRepo.findByUsername(auth.getName());
    }

    public String buildFolderPath(Folder folder) {
        if (folder.getParent() == null) {
            return folder.getName();
        }
        return buildFolderPath(folder.getParent()) + "/" + folder.getName();
    }

    public String createFolder(String name, String parentId) {
        Users user = getCurrentUser();

        Folder parent = null;
        if (parentId != null && !parentId.isEmpty()) {
            parent = folderRepo.findByIdIs(parentId);
            if (parent == null)
                throw new RuntimeException("Folder is not Found");
            if (!parent.getOwner().getId().equals(user.getId()))
                throw new RuntimeException("User is Not Authorized");
        }

        Folder folder = new Folder();
        folder.setName(name);
        folder.setParent(parent);
        folder.setOwner(user);

        folderRepo.save(folder);

        return "Folder Created Successfully";
    }

    public List<Folder> viewFolder(String parentName) {
        Users user = getCurrentUser();
        String parentId = folderRepo.findByName(parentName);
        List<Folder> folders = folderRepo.findFolderByIdAndOwnerId(parentId, user.getId());
        return folders;
    }

    public void renameFolder(String id, String newname){
        Users user = getCurrentUser();
        Folder folder = folderRepo.findByIdIs(id);
        if(folder == null){
            throw new RuntimeException("Folder not found");
        }
        if(!folder.getOwner().getId().equals(user.getId())){
            throw new RuntimeException("Unauthorized");
        }
        folder.setName(newname);
        folderRepo.save(folder);
    }

    public List<Folder> getFolderTree(String parentId) {
        Users user = getCurrentUser();
        if (parentId != null && !parentId.isEmpty()) {
            return folderRepo.findFolderByIdAndOwnerId(parentId, user.getId());
        } else {
            return folderRepo.findByOwnerIdAndParentIsNull(user.getId());
        }
    }

    public void moveFolder(String id, String parentId){
        Users user = getCurrentUser();
        Folder folder = folderRepo.findByIdIs(id);
        Folder parent = folderRepo.findByIdIs(parentId);
        if(folder == null){
            throw new RuntimeException("Folder is not valid");
        }
        if(!folder.getOwner().getId().equals(user.getId())){
            throw new RuntimeException("Unauthorized");
        }

        folder.setParent(parent);
        folderRepo.save(folder);
    }

    @Transactional
    public void deleteFolder(String folderId) {
        Users user = getCurrentUser();
        Folder folder = folderRepo.findByIdIs(folderId);

        if (folder == null) {
            throw new RuntimeException("Folder not found");
        }
        if (!folder.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        recursiveDelete(folder, user);
        folderRepo.delete(folder);
    }

    private void recursiveDelete(Folder folder, Users user) {
        // Delete subfolders recursively
        if (folder.getSubFolders() != null) {
            for (Folder subFolder : folder.getSubFolders()) {
                recursiveDelete(subFolder, user);
            }
        }

        // Delete files in this folder
        if (folder.getFiles() != null) {
            for (FileEntity file : folder.getFiles()) {
                try {
                    Path filePath = Path.of(file.getPath());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    // Log error but continue deleting other things
                    System.err.println("Could not delete physical file: " + file.getPath());
                }
                user.setUsedStorage(user.getUsedStorage() - file.getSize());
            }
        }
        usersRepo.save(user);
    }
}
