package com.jung.creatorlink.dto.tracking;

import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.tracking.TrackingLink;
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
    private Status status;

    public static TrackingLinkResponse from(TrackingLink link) {
        return TrackingLinkResponse.builder()
                .id(link.getId())
                .campaignId(link.getCampaign().getId())
                .creatorId(link.getCreator().getId())
                .channelId(link.getChannel().getId())
                // channelDisplay는 일단 null로 두거나, Channel에 display용 메서드 있으면 여기서 채우기
                .channelDisplay(null)
                .slug(link.getSlug())
                .finalUrl(link.getFinalUrl())
                .status(link.getStatus())
                .build();
    }

}
