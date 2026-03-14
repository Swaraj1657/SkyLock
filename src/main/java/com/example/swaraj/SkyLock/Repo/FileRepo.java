package com.example.swaraj.SkyLock.Repo;

import com.example.swaraj.SkyLock.Models.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepo extends JpaRepository<FileEntity, String> {
    

    List<FileEntity> findByOwnerIdAndFolderIsNull(String ownerId);

    List<FileEntity> findByOwnerId(String ownerId);

    List<FileEntity> findByFolderIdAndOwnerId(String folderId, String ownerId);

    Optional<FileEntity> findById(String fileId);

    FileEntity findByIdIs(String id);

    FileEntity findByFilenameAndOwnerId(String filename, String ownerId);
}
