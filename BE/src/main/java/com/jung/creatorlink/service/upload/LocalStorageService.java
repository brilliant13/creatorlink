package com.jung.creatorlink.service.upload;

import com.jung.creatorlink.dto.upload.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService{
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public UploadResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어있습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        // 확장자 추출 (없으면 기본 png 처리)
        String originalName = file.getOriginalFilename();
        String ext = "png";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }

        String storedName = java.util.UUID.randomUUID() + "." + ext;

        try {
            java.nio.file.Path dirPath = java.nio.file.Paths.get(uploadDir);
            java.nio.file.Files.createDirectories(dirPath);

            java.nio.file.Path target = dirPath.resolve(storedName);
            file.transferTo(target.toFile());

            // 이 URL은 WebConfig의 ResourceHandler와 매칭되어야 함
            String url = "/uploads/" + storedName;

            return new UploadResponse(url, originalName, file.getSize(), contentType);
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}
