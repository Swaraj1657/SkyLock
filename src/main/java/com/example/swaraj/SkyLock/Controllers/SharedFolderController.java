package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.DTO.ShareRequest;
import com.example.swaraj.SkyLock.Services.SharedFolderServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SharedFolderController {
    private final SharedFolderServices sharedFolderServices;

    public SharedFolderController(SharedFolderServices sharedFolderServices) {
        this.sharedFolderServices = sharedFolderServices;
    }

    @PostMapping("/share/folder/{id}")
    public ResponseEntity<?> sharedFolder(@PathVariable String id,
                                          @RequestBody ShareRequest request){
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
}
