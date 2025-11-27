package com.jung.creatorlink.dto.creator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreatorCreateRequest {

    @NotNull
    //유효성 검사. 검증 애노테이션
    private Long advertiserId; //광고주 ID (지금은 인증 없으니 쿼리로 받음)

    @NotBlank
    private String name;

    @NotBlank
    private String channelName;

    @NotBlank
    private String channelUrl;

    private String note;
}
