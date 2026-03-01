package com.example.swaraj.SkyLock.Controllers;

import com.example.swaraj.SkyLock.Services.ChunkUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class ChunkUploadController {

    private final ChunkUploadService chunkUploadService;

    public ChunkUploadController(ChunkUploadService chunkUploadService) {
        this.chunkUploadService = chunkUploadService;
    }


    @PostMapping("/upload-chunk")
    public ResponseEntity<?> uploadChunk(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam(value = "folderId", required = false) String folderId) {

        try {
            String result = chunkUploadService.saveChunk(chunk, fileName, chunkNumber, totalChunks);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to save chunk: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/merge-file")
    public ResponseEntity<?> mergeFile(
            @RequestParam("fileName") String fileName,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam(value = "folderId", required = false) String folderId) {

        try {
            String result = chunkUploadService.mergeChunks(fileName, totalChunks, folderId);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Merge failed: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
