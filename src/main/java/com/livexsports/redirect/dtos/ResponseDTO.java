package com.livexsports.redirect.dtos;

import java.time.LocalDateTime;

public class ResponseDTO {
    private LocalDateTime downloadedAt = LocalDateTime.now();
    private String response;

    public LocalDateTime getDownloadedAt() {
        return downloadedAt;
    }

    public void setDownloadedAt(LocalDateTime downloadedAt) {
        this.downloadedAt = downloadedAt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
