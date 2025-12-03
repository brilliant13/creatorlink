package com.jung.creatorlink.dto.creator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreatorUpdateRequest {
    @NotNull
    private Long advertiserId; // 소유자 확인용

    @NotBlank
    private String name;

    @NotBlank
    private String channelName;

    @NotBlank
    private String channelUrl;

    private String note;
}
