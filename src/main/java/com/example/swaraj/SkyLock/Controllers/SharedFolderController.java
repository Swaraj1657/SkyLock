package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.DTO.ShareRequest;
import com.example.swaraj.SkyLock.Models.Folder;
import com.example.swaraj.SkyLock.Services.FolderServices;
import com.example.swaraj.SkyLock.Services.SharedFolderServices;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SharedFolderController {
    private final SharedFolderServices sharedFolderServices;
    private final FolderServices folderServices;

    public SharedFolderController(SharedFolderServices sharedFolderServices, FolderServices folderServices) {
        this.sharedFolderServices = sharedFolderServices;
        this.folderServices = folderServices;
    }

    @PostMapping("/share/folder/{id}")
    public ResponseEntity<?> sharedFolder(@PathVariable String id,
                                          @RequestBody ShareRequest request) throws MessagingException, IOException {
        try {
            sharedFolderServices.giveAccess(id,request.getEmail(),request.getRole());
            return ResponseEntity.ok(Map.of("message", "Access is given"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/share/folder/{id}")
    public ResponseEntity<?> getShares(@PathVariable String id) {
        try {
            Folder folder = folderServices.findByIdIs(id);
            String generalAccess = folder != null ? folder.getGeneralAccess() : "restricted";
            return ResponseEntity.ok(Map.of(
                "shares", sharedFolderServices.getSharesForFolder(id),
                "generalAccess", generalAccess
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/share/folder/{id}/general-access")
    public ResponseEntity<?> updateGeneralAccess(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            sharedFolderServices.updateGeneralAccess(id, body.get("access"));
            return ResponseEntity.ok(Map.of("message", "General access updated"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/share/folder/{id}/copy-link")
    public ResponseEntity<?> copyLink(@PathVariable String id, jakarta.servlet.http.HttpServletRequest request) {
        String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        String link = baseUrl + "/shared/folder/" + id;
        return ResponseEntity.ok(Map.of("link", link));
    }

    @PutMapping("/share/folder/{folderId}/{shareId}")
    public ResponseEntity<?> updateShare(@PathVariable String folderId, @PathVariable String shareId, @RequestBody Map<String, String> body) {
        try {
            sharedFolderServices.updateShareRole(shareId, body.get("role"));
            return ResponseEntity.ok(Map.of("message", "Role updated"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/share/folder/{folderId}/{shareId}")
    public ResponseEntity<?> removeShare(@PathVariable String folderId, @PathVariable String shareId) {
        try {
            sharedFolderServices.removeShare(shareId);
            return ResponseEntity.ok(Map.of("message", "Access removed"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("user is not Authorized")) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
