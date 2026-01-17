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
    private String displayName; // 추가(비워도 되게 하고 서버가 기본값 생성 가능)
    private String iconUrl;     // 추가(선택)
}