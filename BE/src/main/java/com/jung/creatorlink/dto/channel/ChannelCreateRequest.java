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

    private String displayName; //optional
    private String iconUrl;     //업로드 API에서 받은 URL

}