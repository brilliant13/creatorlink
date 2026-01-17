package com.jung.creatorlink.service.tracking;

import com.jung.creatorlink.domain.campaign.Campaign;
import com.jung.creatorlink.domain.channel.Channel;
import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.creator.Creator;
import com.jung.creatorlink.domain.tracking.ClickLog;
import com.jung.creatorlink.domain.tracking.TrackingLink;
import com.jung.creatorlink.dto.tracking.TrackingLinkCreateRequest;
import com.jung.creatorlink.dto.tracking.TrackingLinkResponse;
import com.jung.creatorlink.repository.campaign.CampaignRepository;
import com.jung.creatorlink.repository.channel.ChannelRepository;
import com.jung.creatorlink.repository.creator.CreatorRepository;
import com.jung.creatorlink.repository.tracking.ClickLogRepository;
import com.jung.creatorlink.repository.tracking.TrackingLinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackingLinkService {

    private final TrackingLinkRepository trackingLinkRepository;
    private final ClickLogRepository clickLogRepository;
    private final CampaignRepository campaignRepository;
    private final CreatorRepository creatorRepository;
    private final ChannelRepository channelRepository;

    //1) 트래킹 링크 생성
    public TrackingLinkResponse createTrackingLink(TrackingLinkCreateRequest request) {

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("캠패인을 찾을 수 없습니다."));

        Creator creator = creatorRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("크리에이터를 찾을 수 없습니다."));

        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));


        //  (권장) 요청 advertiserId를 받는다면 "현재 광고주" 검증까지
        // JWT 붙기 전이라면 request.getAdvertiserId()를 받는 게 실수 방지에 도움 됨
        if (request.getAdvertiserId() != null) {
            Long advId = request.getAdvertiserId();
            if (!campaign.getAdvertiser().getId().equals(advId)
                    || !creator.getAdvertiser().getId().equals(advId)
                    || !channel.getAdvertiser().getId().equals(advId)) {
                throw new IllegalArgumentException("현재 로그인 된 광고주의 리소스만 사용 가능합니다.");
            }
        }
        //  (필수) 세 리소스 소유권이 같은지 검증
        Long advIdFromCampaign = campaign.getAdvertiser().getId();
        if (!creator.getAdvertiser().getId().equals(advIdFromCampaign)
                || !channel.getAdvertiser().getId().equals(advIdFromCampaign)) {
            throw new IllegalArgumentException("같은 광고주의 캠페인/크리에이터/채널만 트래킹 링크로 연결할 수 있습니다.");
        }

        //slug 생성 (중복 체크 포함)
        String slug = generateUniqueSlug();

        String finalUrl = request.getFinalUrl();
        if (finalUrl == null || finalUrl.isBlank()) {
            //요청에서 finalUrl을 안 주면 캠페인의 landingUrl 사용
            finalUrl = campaign.getLandingUrl();
        }

        TrackingLink trackingLink = TrackingLink.builder()
                .campaign(campaign)
                .creator(creator)
                .channel(channel)
                .slug(slug)
                .finalUrl(finalUrl)
                .createdAt(LocalDateTime.now())
                .status(Status.ACTIVE)//이거 추가
                .build();

        TrackingLink saved = trackingLinkRepository.save(trackingLink);

        return TrackingLinkResponse.builder()
                .id(saved.getId())
                .campaignId(saved.getCampaign().getId())
                .creatorId(saved.getCreator().getId())
                .channelId(saved.getChannel().getId())
                .slug(saved.getSlug())
                .finalUrl(saved.getFinalUrl())
                .build();
    }

    public String handleClick(String slug, HttpServletRequest request) {

        //  기존: findBySlug(...)
        // TrackingLink trackingLink = trackingLinkRepository.findBySlug(slug)
        //        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 링크입니다."));

        //  변경: ACTIVE 인 slug 만 허용
        TrackingLink trackingLink = trackingLinkRepository
                .findBySlugAndStatus(slug, Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 링크입니다."));

        // 여기까지 왔다는 건 이미 ACTIVE 라는 뜻이라 isActive() 체크는 사실 필요 없음
        // 그래도 방어적으로 남기고 싶으면 유지해도 되고, 안 써도 됨.

        ClickLog log = ClickLog.builder()
                .trackingLink(trackingLink)
                .clickedAt(LocalDateTime.now())
                .ip(extractClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .referer(request.getHeader("Referer"))
                .build();

        clickLogRepository.save(log);

        return trackingLink.getFinalUrl();
    }

    // 캠페인별 트래킹 링크 목록 조회
    public List<TrackingLinkResponse> getTrackingLinksByCampaign(Long campaignId) {

        //  기존
        // List<TrackingLink> links = trackingLinkRepository.findAllByCampaignId(campaignId);

        //  변경: ACTIVE 인 링크만 대시보드에 보여주기
        List<TrackingLink> links =
                trackingLinkRepository.findAllByCampaign_IdAndStatus(campaignId, Status.ACTIVE);

        return links.stream()
                .map(link -> TrackingLinkResponse.builder()
                        .id(link.getId())
                        .campaignId(link.getCampaign().getId())
                        .creatorId(link.getCreator().getId())
                        .channelId(link.getChannel().getId())
                        .slug(link.getSlug())
                        .finalUrl(link.getFinalUrl())
                        .build()
                )
                .toList();
    }


    //slug 생성기 (간단 버전)
    private String generateUniqueSlug() {
        String slug;
        do {
            slug = randomAlphaNumeric(8);
        } while (trackingLinkRepository.existsBySlug(slug));
        return slug;
    }

    private String randomAlphaNumeric(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String extractClientIp(HttpServletRequest request) {
        //프록시 등 고려하면 X-Forwarded-For 등을 봐야 하지만, 지금은 간단 버전으로 만든다.
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    //soft delete
    @Transactional
    public void deleteTrackingLink(Long id) {
        TrackingLink link = trackingLinkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트래킹 링크입니다."));

        link.deactivate();
    }

}
