package com.jung.creatorlink.loadTest;

import lombok.Data;

@Data
public class SeedRequest {
    //Request DTO
    private String userEmail = "seed@creatorlink.com";
    private String userName = "Seed User";
    private String passwordHash = "seed"; // 운영용 아님. 그냥 더미

    private int campaigns = 1;
    private int creators = 50;
    private int channels = 20;

    // 핵심: 링크 개수 = creators * linksPerCreator
    private int linksPerCreator = 20; // 예: 50*20=1000 links

    private String landingUrl = "https://example.com";
}
