package com.jung.creatorlink.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChannelRankingResponse {
    private Long channelId;
    private String channelDisplayName;
    private long clicks;
}
