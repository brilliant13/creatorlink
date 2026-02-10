package com.jung.creatorlink.loadTest;

import lombok.Data;

@Data
public class SeedRequest {
    //부하 테스트용 기본 시드 생성 요청 DTO
    //(/api/test/seed 에서 사용)

    //Request DTO
    //(1) 광고주(User) 생성/조회에 쓰는 값
    private String userEmail = "seed@creatorlink.com";
    private String userName = "Seed User";
    private String passwordHash = "seed"; // 운영용 아님. 그냥 더미

    //(2) 캠페인/크리에이터/채널 생성 개수
    private int campaigns = 1;
    private int creators = 50;
    private int channels = 20;

    //(3) 트래킹 링크 생성 개수
    // 핵심: 링크 개수 = creators * linksPerCreator
    private int linksPerCreator = 20; // 예: 50*20=1000 links

    //(4) 트래킹 링크가 최종적으로 리다이렉트할 URL
    private String landingUrl = "https://example.com";

    //(5) tracking_links soft_delete E2개선(tracking_links에 )효과 위해 inactive/active 조합
    //tracking_links(campaign_id/creator_id/channel_id , status) 복합인덱스
    private double inactiveLinkRatio = 0.6; // 0.0~0.99 (예: 0.8이면 80% INACTIVE)

}
