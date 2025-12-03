package com.jung.creatorlink.dto.creator;

import com.jung.creatorlink.domain.creator.Creator;
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

    // 엔티티 -> DTO 변환 메서드
    public static CreatorResponse from(Creator creator) {
        return CreatorResponse.builder()
                .id(creator.getId())
                .advertiserId(creator.getAdvertiser().getId())
                .name(creator.getName())
                .channelName(creator.getChannelName())
                .channelUrl(creator.getChannelUrl())
                .note(creator.getNote())
                .build();
    }
}
