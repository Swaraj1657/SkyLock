package com.example.swaraj.SkyLock.Repo;

import com.example.swaraj.SkyLock.Models.Folder;
import com.example.swaraj.SkyLock.Models.SharedFolder;
import com.example.swaraj.SkyLock.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedFolderRepo extends JpaRepository<SharedFolder,String> {
    List<SharedFolder> findBySharedWith(Users sharedWith);

    boolean existsByFolderAndSharedWith(Folder folder, Users sharedWith);
    List<SharedFolder> findByFolder(Folder folder);
}
