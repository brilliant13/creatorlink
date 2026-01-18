package com.jung.creatorlink.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUpdateRequest {
    @NotNull
    private Long advertiserId;

    @NotBlank
    private String platform;

    @NotBlank
    private String placement;

    private String note;
    private String displayName; //optional
    private String iconUrl;     //업로드 API에서 받은 URL
}