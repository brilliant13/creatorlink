package com.jung.creatorlink.dto.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChannelPatchRequest {

    @NotNull
    private Long advertiserId;   // 지금은 인증 없으니 유지

    private String platform;     // optional
    private String placement;    // optional
    private String displayName;  // optional
    private String iconUrl;      // optional
    private String note;         // optional
}

