package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Models.Users;
import com.example.swaraj.SkyLock.Repo.FileRepo;
import com.example.swaraj.SkyLock.Repo.UsersRepo;
import com.example.swaraj.SkyLock.Services.DashBoardService;
import com.example.swaraj.SkyLock.Services.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DashBoardController {

    private final DashBoardService dashBoardService;
    private final FileRepo fileRepo;
    private final UsersRepo usersRepo;
    private final FileService fileService;

    public DashBoardController(DashBoardService dashBoardService, FileRepo fileRepo, UsersRepo usersRepo,
            FileService fileService) {
        this.dashBoardService = dashBoardService;
        this.fileRepo = fileRepo;
        this.usersRepo = usersRepo;
        this.fileService = fileService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam(required = false) String folderId) {
        Map<String, Object> data = dashBoardService.getDashboardData(folderId);
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileId) {

        try {

            fileService.deleteFile(fileId);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "File deleted successfully"));

        } catch (RuntimeException e) {

            if (e.getMessage()
                    .equals("File not found")) {
                return ResponseEntity.notFound().build();
            }

            if (e.getMessage()
                    .equals("Unauthorized")) {
                return ResponseEntity
                        .status(403)
                        .body("Unauthorized");
            }

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<?> downloadFile(
            @PathVariable String fileId)
            throws MalformedURLException {

        try {
            Resource resource = fileService.downloadFile(fileId);

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.APPLICATION_OCTET_STREAM)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" +
                                    resource.getFilename() + "\"")
                    .body(resource);

        } catch (RuntimeException e) {

            if (e.getMessage()
                    .equals("File not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage()
                    .equals("Unauthorized")) {
                return ResponseEntity
                        .status(403)
                        .body("Unauthorized");
            }
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
