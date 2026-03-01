package com.example.swaraj.SkyLock.Services;

import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Models.Folder;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.FileRepo;
import com.example.swaraj.SkyLock.Repo.FolderRepo;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.LocalDateTime;

@Service
public class ChunkUploadService {

    private final UsersRepo usersRepo;
    private final FileRepo fileRepo;
    private final FolderRepo folderRepo;
    private final FolderServices folderServices;

    @Value("${file.storage.path}")
    private String storagePath;

    @Value("${file.temp.path}")
    private String tempPath;

    public ChunkUploadService(UsersRepo usersRepo, FileRepo fileRepo,
            FolderRepo folderRepo, FolderServices folderServices) {
        this.usersRepo = usersRepo;
        this.fileRepo = fileRepo;
        this.folderRepo = folderRepo;
        this.folderServices = folderServices;
    }


    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usersRepo.findByUsername(auth.getName());
    }


    public String saveChunk(MultipartFile chunk, String fileName,
            int chunkNumber, int totalChunks) throws IOException {

        Users user = getCurrentUser();
        if (user == null)
            throw new RuntimeException("User not authenticated");


        String safeName = StringUtils.cleanPath(fileName);


        Path chunkDir = Path.of(tempPath, "user_" + user.getId(), safeName);
        Files.createDirectories(chunkDir);


        Path chunkFile = chunkDir.resolve("chunk_" + chunkNumber);
        Files.copy(chunk.getInputStream(), chunkFile, StandardCopyOption.REPLACE_EXISTING);

        return "Chunk " + chunkNumber + " of " + totalChunks + " uploaded";
    }


    @Transactional
    public String mergeChunks(String fileName,
                              int totalChunks,
                              String folderId) throws IOException {

        Users user = getCurrentUser();
        if (user == null)
            throw new RuntimeException("User not authenticated");

        String safeName = StringUtils.cleanPath(fileName);

        Path chunkDir =
                Path.of(tempPath,
                        "user_" + user.getId(),
                        safeName);


        for (int i = 0; i < totalChunks; i++) {
            if (!Files.exists(chunkDir.resolve("chunk_" + i))) {
                throw new RuntimeException(
                        "Missing chunk " + i + " — upload incomplete");
            }
        }


        Path basePath =
                Path.of(storagePath, "user_" + user.getId());

        Path finalDir = basePath;
        Folder folder = null;

        if (StringUtils.hasText(folderId)) {

            folder = folderRepo.findByIdIs(folderId);

            if (folder == null)
                throw new RuntimeException("Folder does not exist");

            if (!folder.getOwner().getId().equals(user.getId()))
                throw new RuntimeException("Unauthorized access");

            String relativePath =
                    folderServices.buildFolderPath(folder);

            finalDir = basePath.resolve(relativePath);
        }

        Files.createDirectories(finalDir);

        Path finalFile = finalDir.resolve(safeName);



        try (FileChannel outputChannel =
                     FileChannel.open(
                             finalFile,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.WRITE,
                             StandardOpenOption.TRUNCATE_EXISTING)) {

            long position = 0;

            for (int i = 0; i < totalChunks; i++) {

                Path chunkFile =
                        chunkDir.resolve("chunk_" + i);

                try (FileChannel inputChannel =
                             FileChannel.open(
                                     chunkFile,
                                     StandardOpenOption.READ)) {

                    long size = inputChannel.size();

                    long transferred = 0;

                    while (transferred < size) {
                        transferred += inputChannel.transferTo(
                                transferred,
                                size - transferred,
                                outputChannel.position(position)
                        );
                    }

                    position += size;
                }
            }
        }



        long fileSize = Files.size(finalFile);

        if (user.getUsedStorage() + fileSize >
                user.getMaxStorage()) {

            Files.deleteIfExists(finalFile);
            cleanupChunkDir(chunkDir);

            throw new RuntimeException("Storage limit exceeded");
        }


        FileEntity fileEntity = FileEntity.builder()
                .filename(safeName)
                .path(finalFile.toString())
                .size(fileSize)
                .owner(user)
                .folder(folder)
                .uploadedAt(LocalDateTime.now())
                .build();

        fileRepo.save(fileEntity);

        user.setUsedStorage(
                user.getUsedStorage() + fileSize);

        usersRepo.save(user);

        cleanupChunkDir(chunkDir);

        return "File uploaded successfully";
    }


    private void cleanupChunkDir(Path chunkDir) {
        try {
            if (Files.exists(chunkDir)) {

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(chunkDir)) {
                    for (Path entry : stream) {
                        Files.deleteIfExists(entry);
                    }
                }
                Files.deleteIfExists(chunkDir);
            }
        } catch (IOException e) {

            System.err.println("Warning: could not clean temp chunks at " + chunkDir + ": " + e.getMessage());
        }
    }
}
