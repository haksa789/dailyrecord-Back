package com.dailyrecord.backend.dto;

public class FileUploadResponse {
    private String message;
    private String fileName;
    private Long photoId; // photoId 추가

    public FileUploadResponse(String message, String fileName, Long photoId) {
        this.message = message;
        this.fileName = fileName;
        this.photoId = photoId; // 초기화
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }
}