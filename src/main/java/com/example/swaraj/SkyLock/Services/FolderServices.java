package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.Folder;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.FolderRepo;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderServices {

    private UsersRepo usersRepo;
    private FolderRepo folderRepo;


    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    Users user = usersRepo.findByUsername(username);

    public FolderServices(FolderRepo folderRepo, UsersRepo usersRepo) {
        this.folderRepo = folderRepo;
        this.usersRepo = usersRepo;
    }

    public String buildFolderPath(Folder folder){
        if(folder.getParent() != null){
            return folder.getName();
        }
        return buildFolderPath(folder.getParent()) + "/" + folder.getName();
    }


    public String createFolder(String name, String parentName){

        Folder parent = null;
        String parentId = folderRepo.findByName(parentName);
        if(parentId != null){
            parent = folderRepo.findByIdIs(parentId);
            if(parent == null)
                throw new RuntimeException("Folder is not Found");
            if(!parent.getOwner().getId().equals(user.getId()))
                throw new RuntimeException("User is Not Authorized");
        }

        Folder folder = new Folder();
        folder.setName(name);
        folder.setParent(parent);
        folder.setOwner(user);

        folderRepo.save(folder);

        return "Folder Created Sucessfully";
    }

    public List<Folder> viewFolder(String parentName){
        String parentId = folderRepo.findByName(parentName);
        List<Folder> folders = folderRepo.findFolderByIdAndUser(parentId,user.getId());
        return folders;
    }
}
