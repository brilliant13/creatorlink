package com.jung.creatorlink.controller.upload;

import com.jung.creatorlink.dto.upload.UploadResponse;
import com.jung.creatorlink.service.upload.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Tag(name = "UploadController", description = "파일 업로드 API")
public class UploadController {
    private final StorageService storageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "이미지 업로드", description = "이미지 파일을 업로드하고 접근 가능한 URL을 반환한다.")
    public UploadResponse upload(@RequestPart("file") MultipartFile file) {
        return storageService.store(file);
    }
}
