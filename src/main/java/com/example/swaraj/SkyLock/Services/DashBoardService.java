package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Repo.FolderRepo;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashBoardService {

    private UsersRepo usersRepo;
    private FolderRepo folderRepo;


    public ResponseEntity<?> displayAssets(String parentId){

    }
}
