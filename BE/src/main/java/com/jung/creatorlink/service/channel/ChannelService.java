package com.jung.creatorlink.service.channel;


import com.jung.creatorlink.domain.channel.Channel;
import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.user.User;
import com.jung.creatorlink.dto.channel.ChannelCreateRequest;
import com.jung.creatorlink.dto.channel.ChannelResponse;
import com.jung.creatorlink.dto.channel.ChannelUpdateRequest;
import com.jung.creatorlink.repository.channel.ChannelRepository;
import com.jung.creatorlink.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    // 채널 생성
    public ChannelResponse createChannel(ChannelCreateRequest request) {

        // 1) 광고주 존재 여부 확인
        User advertiser = userRepository.findById(request.getAdvertiserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 광고주입니다."));

        // 2) 같은 광고주 내에서 platform+placement 중복 방지
        if (channelRepository.existsByAdvertiser_IdAndPlatformAndPlacement(
                request.getAdvertiserId(), request.getPlatform(), request.getPlacement())) {
            throw new IllegalArgumentException("이미 같은 채널(플랫폼+노출위치)이 존재합니다.");
        }

        String displayName = (request.getDisplayName() == null || request.getDisplayName().isBlank())
                ? Channel.defaultDisplayName(request.getPlatform(), request.getPlacement())
                : request.getDisplayName();

        // 3) Channel 생성
        Channel channel = Channel.builder()
                .advertiser(advertiser)
                .platform(request.getPlatform())
                .placement(request.getPlacement())
                .displayName(displayName)
                .iconUrl(request.getIconUrl())
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();

        // 4) 저장 후 Response 변환
        Channel saved = channelRepository.save(channel);
        return ChannelResponse.from(saved);
    }

    // 광고주별 채널 목록 조회 (ACTIVE만)
    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannelsByAdvertiser(Long advertiserId) {
        List<Channel> channels =
                channelRepository.findAllByAdvertiser_IdAndStatus(advertiserId, Status.ACTIVE);

        return channels.stream()
                .map(ChannelResponse::from)
                .toList();
    }

    // 채널 단건 조회 (소유권 검증)
    @Transactional(readOnly = true)
    public ChannelResponse getChannel(Long channelId, Long advertiserId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));

        if (!channel.getAdvertiser().getId().equals(advertiserId)) {
            throw new IllegalArgumentException("해당 광고주의 채널이 아닙니다.");
        }

        // (정책) 삭제된 채널이면 조회 막고 싶으면 아래 추가
        if (!channel.isActive()) {
            throw new IllegalArgumentException("삭제된 채널입니다.");
        }

        return ChannelResponse.from(channel);
    }

    // 채널 수정 (소유권 + 중복 검증)
    @Transactional
    public ChannelResponse updateChannel(Long channelId, ChannelUpdateRequest request) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));

        // 소유권 검증
        if (!channel.getAdvertiser().getId().equals(request.getAdvertiserId())) {
            throw new IllegalArgumentException("이 채널을 수정할 권한이 없습니다.");
        }

        // (선택) 삭제된 채널 수정 방지
        if (!channel.isActive()) {
            throw new IllegalArgumentException("삭제된 채널은 수정할 수 없습니다.");
        }

        // 같은 광고주 내 platform+placement 중복 방지 (자기 자신 제외)
        boolean exists = channelRepository.existsByAdvertiser_IdAndPlatformAndPlacementAndIdNot(
                request.getAdvertiserId(),
                request.getPlatform(),
                request.getPlacement(),
                channelId
        );
        if (exists) {
            throw new IllegalArgumentException("이미 같은 채널(플랫폼+노출위치)이 존재합니다.");
        }

        // 엔티티 헬퍼 메서드로 수정
//        channel.update(request.getPlatform(), request.getPlacement(), request.getNote());

        String displayName = (request.getDisplayName() == null || request.getDisplayName().isBlank())
                ? Channel.defaultDisplayName(request.getPlatform(), request.getPlacement())
                : request.getDisplayName();
        channel.update(
                request.getPlatform(),
                request.getPlacement(),
                displayName,
                request.getIconUrl(),
                request.getNote()
        );

        return ChannelResponse.from(channel);
    }

    // 채널 삭제 (Soft delete)
    @Transactional
    public void deleteChannel(Long channelId, Long advertiserId) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));

        // 소유권 검증
        if (!channel.getAdvertiser().getId().equals(advertiserId)) {
            throw new IllegalArgumentException("이 채널을 삭제할 권한이 없습니다.");
        }

        channel.deactivate();
    }

}
