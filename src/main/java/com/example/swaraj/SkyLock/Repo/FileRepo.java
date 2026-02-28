package com.example.swaraj.SkyLock.Repo;


import com.example.swaraj.SkyLock.Models.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepo extends JpaRepository<FileEntity, String> {
    String findByName(String id);

    @Query("""
        SELECT f 
        FROM FileEntity f 
        WHERE f.folder.id = :parentId
        AND f.owner.id = :userID
""")
    List<FileEntity>findFilesByIdAndUser(
            @Param("parentId") String parentId,
            @Param("userId") String userId
    );


}
