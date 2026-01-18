package com.jung.creatorlink.service.upload;

import com.jung.creatorlink.dto.upload.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    UploadResponse store(MultipartFile file);
}
