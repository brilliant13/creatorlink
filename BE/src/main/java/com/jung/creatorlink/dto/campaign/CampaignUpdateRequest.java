package com.jung.creatorlink.dto.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CampaignUpdateRequest {
    @NotNull
    private Long advertiserId; // 소유자 확인용 (임시, 나중에 JWT로 대체)

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String landingUrl;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
