package com.jung.creatorlink.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadResponse {
    private String url;          // 클라이언트가 바로 <img src="...">로 사용
    private String originalName;
    private long size;
    private String contentType;
}

