package com.jung.creatorlink.controller.tracking;

import com.jung.creatorlink.dto.tracking.TrackingLinkCreateRequest;
import com.jung.creatorlink.dto.tracking.TrackingLinkResponse;
import com.jung.creatorlink.service.tracking.TrackingLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking-links")
@RequiredArgsConstructor
@Tag(name = "TrackingLinkController", description = "트래킹 링크 발급 API")

// 생성 API
public class TrackingLinkController {

    private final TrackingLinkService trackingLinkService;

    //트래킹 링크 발급
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "트랙킹 링크 발급", description = "")
    public TrackingLinkResponse createTrackingLink(@Valid @RequestBody TrackingLinkCreateRequest request) {
        return trackingLinkService.createTrackingLink(request);
    }

    //나중에 목록 조회, 단건 조회 등도 여기에 추가 가능
}
