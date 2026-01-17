package com.jung.creatorlink.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TrackingLinkResponse {

    private Long id;
    private Long campaignId;
    private Long creatorId;
    private Long channelId;
    private String channelDisplay; // "Instagram / Story" 같은 표시용 (선택)
    private String slug;
    private String finalUrl;
}
