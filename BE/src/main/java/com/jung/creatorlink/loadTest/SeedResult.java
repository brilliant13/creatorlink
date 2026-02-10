package com.jung.creatorlink.loadTest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
//@AllArgsConstructor
@Builder
public class SeedResult {
    //Result DTO
    private long userId;
    private int campaigns;
    private int creators;
    private int channels;

    //private int trackingLinks;
    private int trackingLinksTotal;
    private int trackingLinksActive;
    private int trackingLinksInactive;

    private double inactiveLinkRatioApplied;

    //다음 단계 seed-clicklogs에 바로 쓸 값
    private long campaignId;
}
