package com.jung.creatorlink.dto.tracking;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrackingLinkCreateRequest {

    @NotNull
    private Long campaignId;

    @NotNull
    private Long creatorId;

    // finalUrl을 따로 지정하고 싶으면 사용.
    // 비워두면 Campaign의 landingUrl을 쓸 수도 있음. (Service에서 처리)
    private String finalUrl;
}
