package com.example.swaraj.SkyLock.Repo;

import com.example.swaraj.SkyLock.Models.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepo extends JpaRepository<FileEntity, String> {

}
