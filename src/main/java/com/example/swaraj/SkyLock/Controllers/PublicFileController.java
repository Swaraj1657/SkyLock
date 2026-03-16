package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.Models.FileEntity;
import com.example.swaraj.SkyLock.Repo.FileRepo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
public class PublicFileController {

    private final FileRepo fileRepo;

    public PublicFileController(FileRepo fileRepo) {
        this.fileRepo = fileRepo;
    }

    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<?> downloadSharedFile(@PathVariable String fileId) {
        try {
            Optional<FileEntity> opt = fileRepo.findById(fileId);
            if (opt.isEmpty()) return ResponseEntity.notFound().build();
            
            FileEntity file = opt.get();
            if (!"anyone".equals(file.getGeneralAccess())) {
                return ResponseEntity.status(403).body("Unauthorized: Link sharing is not enabled for this file.");
            }

            Path filePath = Path.of(file.getPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(404).body("Physical file missing");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body("Error parsing file path");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/files/preview/{fileId}")
    public ResponseEntity<?> previewSharedFile(@PathVariable String fileId) {
        try {
            Optional<FileEntity> opt = fileRepo.findById(fileId);
            if (opt.isEmpty()) return ResponseEntity.notFound().build();
            
            FileEntity file = opt.get();
            if (!"anyone".equals(file.getGeneralAccess())) {
                return ResponseEntity.status(403).body("Unauthorized: Link sharing is not enabled for this file.");
            }

            Path filePath = Path.of(file.getPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(404).body("Physical file missing");
            }
            
            String filename = file.getFilename().toLowerCase();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (filename.endsWith(".pdf")) {
                mediaType = MediaType.APPLICATION_PDF;
            } else if (filename.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (filename.endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            } else if (filename.endsWith(".mp4")) {
                mediaType = MediaType.valueOf("video/mp4");
            } else if (filename.endsWith(".webm")) {
                mediaType = MediaType.valueOf("video/webm");
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body("Error parsing file path");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
