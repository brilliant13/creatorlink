package com.jung.creatorlink.controller.channel;

import com.jung.creatorlink.dto.channel.ChannelCreateRequest;
import com.jung.creatorlink.dto.channel.ChannelPatchRequest;
import com.jung.creatorlink.dto.channel.ChannelResponse;
import com.jung.creatorlink.dto.channel.ChannelUpdateRequest;
import com.jung.creatorlink.service.channel.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Tag(name = "ChannelController", description = "채널(Platform+Placement) 생성 및 조회 API")
public class ChannelController {
    private final ChannelService channelService;

    // 채널 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "채널 생성", description = "광고주의 채널(플랫폼+노출위치)을 생성한다.")
    public ChannelResponse createChannel(@Valid @RequestBody ChannelCreateRequest request) {
        return channelService.createChannel(request);
    }

    // 광고주별 채널 목록 조회
    @GetMapping
    @Operation(summary = "광고주별 채널 목록 조회", description = "광고주가 보유한 ACTIVE 채널 목록을 조회한다.")
    public List<ChannelResponse> getChannels(@RequestParam Long advertiserId) {
        return channelService.getChannelsByAdvertiser(advertiserId);
    }

    // 특정 채널 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "특정 채널 단건 조회", description = "광고주가 소유한 채널 단건을 조회한다.")
    public ChannelResponse getChannel(
            @PathVariable Long id,
            @RequestParam Long advertiserId
    ) {
        return channelService.getChannel(id, advertiserId);
    }

    // 채널 수정
    @PutMapping("/{id}")
    @Operation(summary = "채널 수정", description = "채널 기본 정보(platform, placement, note, displayName, iconUrl)를 수정한다.")
    public ChannelResponse updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody ChannelUpdateRequest request
    ) {
        return channelService.updateChannel(id, request);
    }

    // 채널 부분 수정
    @PatchMapping("/{id}")
    @Operation(summary = "채널 부분 수정", description = "요청에 포함된 필드만 수정한다.")
    public ChannelResponse patchChannel(
            @PathVariable Long id,
            @Valid @RequestBody ChannelPatchRequest request
    ) {
        return channelService.patchChannel(id, request);
    }


    // 채널 삭제 (Soft delete)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "채널 삭제", description = "광고주가 소유한 채널을 삭제(Soft delete)한다.")
    public void deleteChannel(
            @PathVariable Long id,
            @RequestParam Long advertiserId
    ) {
        channelService.deleteChannel(id, advertiserId);
    }

}
