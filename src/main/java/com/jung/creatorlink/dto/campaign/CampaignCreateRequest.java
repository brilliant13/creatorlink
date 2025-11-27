package com.jung.creatorlink.dto.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
//생성 요청 DTO
public class CampaignCreateRequest {

    @NotNull
    private Long advertiserId; //임시: 나중엔 로그인 유저로 대체할 예정
    //지금은 로그인/인증이 없어서 advertiserId를 요청에 같이 받는 방식으로 구현함.
    //나중에 JWT 붙일 때는 여기서 뺴고, Security에서 유저를 꺼내 쓰면 된다.

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String landingUrl;

    private LocalDate startDate;
    private LocalDate endDate;
}
