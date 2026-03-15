package com.example.swaraj.SkyLock.Repo;

import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Models.SharedFile;
import com.example.swaraj.SkyLock.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedFileRepo extends JpaRepository<SharedFile,String> {

    boolean existsByFileAndSharedwith(FileEntity file, Users sharedwith);
    List<SharedFile> findBySharedwith(Users sharedwith);
}
