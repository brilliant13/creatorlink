package com.jung.creatorlink.dto.campaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
//응답 DTO
public class CampaignResponse {
    private Long id;
    private Long advertiserId;
    private String name;
    private String description;
    private String landingUrl;
    private LocalDate startDate;
    private LocalDate endDate;
}
