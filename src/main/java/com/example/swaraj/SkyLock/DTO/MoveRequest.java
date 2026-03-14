package com.example.swaraj.SkyLock.DTO;

public class MoveRequest {
    public void setTargetFolderId(String targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    private String targetFolderId;

    public MoveRequest(String targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    public String getTargetFolderId() {
        return targetFolderId;
    }
}
