package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.DTO.ShareRequest;
import com.example.swaraj.SkyLock.Services.SharedFileService;
import com.example.swaraj.SkyLock.Services.SharedFolderServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SharedFileController {

    private final SharedFileService sharedFileService;
    private final SharedFolderServices sharedFolderServices;

    public SharedFileController(SharedFileService sharedFileService, SharedFolderServices sharedFolderServices) {
        this.sharedFileService = sharedFileService;
        this.sharedFolderServices = sharedFolderServices;
    }

    @PostMapping("/share/file/{id}")
    public ResponseEntity<?> shareFile(@PathVariable String id, @RequestBody ShareRequest request){
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

    @GetMapping("/shared/with-me")
    public ResponseEntity<?> getSharedWithMe() {
        return ResponseEntity.ok(Map.of(
            "sharedFiles", sharedFileService.getSharedWithMe(),
            "sharedFolders", sharedFolderServices.getSharedWithMe()
        ));
    }
}
