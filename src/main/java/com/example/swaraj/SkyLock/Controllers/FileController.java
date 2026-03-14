package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.DTO.MoveRequest;
import com.example.swaraj.SkyLock.DTO.RenameRequest;
import com.example.swaraj.SkyLock.Services.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PutMapping("/files/{id}/rename")
    public ResponseEntity<?> renameFile(@PathVariable String id, @RequestBody RenameRequest request) {
        try {
            fileService.renameFile(id, request.getName());
            return ResponseEntity.ok(Map.of("message", "File renamed successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/files/{id}/move")
    public ResponseEntity<?> moveFile(@PathVariable String id, @RequestBody MoveRequest request){
        try{
            fileService.moveFile(id, request.getTargetFolderId());
            return ResponseEntity.ok(Map.of("message", "File moved successfully"));
        }
        catch (RuntimeException e) {
            if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

