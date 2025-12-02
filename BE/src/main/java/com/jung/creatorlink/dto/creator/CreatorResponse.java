package com.jung.creatorlink.dto.creator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreatorResponse {
    private Long id;
    private Long advertiserId;
    private String name;
    private String channelName;
    private String channelUrl;
    private String note;
}
