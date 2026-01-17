package com.jung.creatorlink.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCreateRequest {

    @NotNull
    private Long advertiserId;   // 지금은 JWT 없으니 일단 받는 방식 OK

    @NotBlank
    private String platform;     // Instagram / YouTube / Blog ...

    @NotBlank
    private String placement;    // Story / Description / Comment ...

    private String note;         // optional

    private String displayName; // 추가(비워도 되게 하고 서버가 기본값 생성 가능)
    private String iconUrl;     // 추가(선택)

}