package com.jung.creatorlink.dto.channel;

import com.jung.creatorlink.domain.channel.Channel;
import com.jung.creatorlink.domain.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ChannelResponse {
    private Long id;
    private Long advertiserId;
    private String platform;
    private String placement;
    private String displayName;
    private String iconUrl;
    private String note;
    private Status status;

    public static ChannelResponse from(Channel ch) {
        return ChannelResponse.builder()
                .id(ch.getId())
                .advertiserId(ch.getAdvertiser().getId())
                .platform(ch.getPlatform())
                .placement(ch.getPlacement())
                .displayName(ch.getDisplayName())
                .iconUrl(ch.getIconUrl())
                .note(ch.getNote())
                .status(ch.getStatus())
                .build();
    }
}