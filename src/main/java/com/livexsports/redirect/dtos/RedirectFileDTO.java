package com.livexsports.redirect.dtos;

import java.io.File;
import java.time.LocalDateTime;

public class RedirectFileDTO {
    private LocalDateTime downloadedAt = LocalDateTime.now();
    private File file;

    public LocalDateTime getDownloadedAt() {
        return downloadedAt;
    }

    public void setDownloadedAt(LocalDateTime downloadedAt) {
        this.downloadedAt = downloadedAt;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
