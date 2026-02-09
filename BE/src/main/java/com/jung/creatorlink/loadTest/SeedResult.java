package com.jung.creatorlink.loadTest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeedResult {
    //Result DTO
    private long userId;
    private int campaigns;
    private int creators;
    private int channels;
    private int trackingLinks;

    //다음 단계 seed-clicklogs에 바로 쓸 값
    private long campaignId;
}
