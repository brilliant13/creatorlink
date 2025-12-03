package com.jung.creatorlink.dto.campaign;

import com.jung.creatorlink.domain.campaign.Campaign;
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

    //엔티티 → DTO 변환 메서드
    public static CampaignResponse from(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .advertiserId(campaign.getAdvertiser().getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .landingUrl(campaign.getLandingUrl())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .build();
    }


}
