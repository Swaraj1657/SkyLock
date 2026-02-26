package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.Folder;
import org.springframework.stereotype.Service;

@Service
public class FolderServices {

    public String buildFolderPath(Folder folder){
        if(folder.getParent() != null){
            return folder.getName();
        }
        return buildFolderPath(folder.getParent()) + "/" + folder.getName();
    }
}
