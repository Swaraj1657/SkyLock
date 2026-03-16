package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.DTO.ShareRequest;
import com.example.swaraj.SkyLock.Services.FileService;
import com.example.swaraj.SkyLock.Services.SharedFileService;
import com.example.swaraj.SkyLock.Services.SharedFolderServices;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SharedFileController {

    private final SharedFileService sharedFileService;
    private final SharedFolderServices sharedFolderServices;
    private final FileService fileService;

    public SharedFileController(SharedFileService sharedFileService, SharedFolderServices sharedFolderServices, FileService fileService) {
        this.sharedFileService = sharedFileService;
        this.sharedFolderServices = sharedFolderServices;
        this.fileService = fileService;
    }

    @PostMapping("/share/file/{id}")
    public ResponseEntity<?> shareFile(@PathVariable String id, @RequestBody ShareRequest request) throws MessagingException, IOException {
        try {
            sharedFileService.giveAccess(id, request.getEmail(), request.getRole());
            return ResponseEntity.ok(Map.of("message", "Access is given"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/share/file/{id}")
    public ResponseEntity<?> getShares(@PathVariable String id) {
        try {
            java.util.Optional<com.example.swaraj.SkyLock.Models.FileEntity> opt = sharedFileService.getCurrentUser() != null ? fileService.findById(id) : java.util.Optional.empty();
            String generalAccess = opt.isPresent() ? opt.get().getGeneralAccess() : "restricted";
            return ResponseEntity.ok(Map.of(
                "shares", sharedFileService.getSharesForFile(id),
                "generalAccess", generalAccess
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/share/file/{id}/general-access")
    public ResponseEntity<?> updateGeneralAccess(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            sharedFileService.updateGeneralAccess(id, body.get("access"));
            return ResponseEntity.ok(Map.of("message", "General access updated"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/share/file/{id}/copy-link")
    public ResponseEntity<?> copyLink(@PathVariable String id, jakarta.servlet.http.HttpServletRequest request) {
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        String link = baseUrl + "/api/public/files/preview/" + id;
        return ResponseEntity.ok(Map.of("link", link));
    }

    @PutMapping("/share/file/{fileId}/{shareId}")
    public ResponseEntity<?> updateShare(@PathVariable String fileId, @PathVariable String shareId, @RequestBody Map<String, String> body) {
        try {
            sharedFileService.updateShareRole(shareId, body.get("role"));
            return ResponseEntity.ok(Map.of("message", "Role updated"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/share/file/{fileId}/{shareId}")
    public ResponseEntity<?> removeShare(@PathVariable String fileId, @PathVariable String shareId) {
        try {
            sharedFileService.removeShare(shareId);
            return ResponseEntity.ok(Map.of("message", "Access removed"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/shared/with-me")
    public ResponseEntity<?> getSharedWithMe() {
        return ResponseEntity.ok(Map.of(
            "sharedFiles", sharedFileService.getSharedWithMe(),
            "sharedFolders", sharedFolderServices.getSharedWithMe()
        ));
    }
}
