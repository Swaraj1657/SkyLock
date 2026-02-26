package com.example.swaraj.SkyLock.Repo;

import com.example.swaraj.SkyLock.Models.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepo extends JpaRepository<Folder,String> {

    Folder findByIdIs(String id); 
}
