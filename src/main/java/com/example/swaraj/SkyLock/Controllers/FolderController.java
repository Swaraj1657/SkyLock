package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.DTO.MoveRequest;
import com.example.swaraj.SkyLock.DTO.RenameRequest;
import com.example.swaraj.SkyLock.Models.Folder;
import com.example.swaraj.SkyLock.Services.FolderServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FolderController {

    private final FolderServices folderServices;

    public FolderController(FolderServices folderServices) {
        this.folderServices = folderServices;
    }

    @PostMapping("/folders")
    public ResponseEntity<?> createFolder(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String parentId = request.get("parentId");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Folder name is required"));
        }

        try {
            String message = folderServices.createFolder(name.trim(), parentId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/folders/{id}/rename")
    public ResponseEntity<?> renameFolder(@PathVariable String id, @RequestBody RenameRequest request) {
        try {
            folderServices.renameFolder(id, request.getName());
            return ResponseEntity.ok(Map.of("message", "Folder renamed successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/folders/tree")
    public ResponseEntity<?> getFolderTree(@RequestParam(required = false) String parentId) {
        try {
            List<Folder> folders = folderServices.getFolderTree(parentId);
            List<Map<String, Object>> folderList = folders.stream().map(f -> Map.<String, Object>of(
                    "id", f.getId(),
                    "name", f.getName()
            )).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("folders", folderList));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/folders/{id}/move")
    public ResponseEntity<?> moveFolder(@PathVariable String id, @RequestBody(required = false) MoveRequest target){
        try {
            folderServices.moveFolder(id, target.getTargetFolderId());
            return ResponseEntity.ok(Map.of("message","File moved successfully"));
        }
        catch (RuntimeException e) {
            if (e.getMessage().equals("Unauthorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
