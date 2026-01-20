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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

        // (필수) 같은 조합 ACTIVE는 1개만
        if (trackingLinkRepository.existsByCampaign_IdAndCreator_IdAndChannel_IdAndStatus(
                campaign.getId(), creator.getId(), channel.getId(), Status.ACTIVE)) {
            throw new IllegalArgumentException("이미 같은 조합의 ACTIVE 트래킹 링크가 존재합니다.");
        }

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


        String finalUrl = request.getFinalUrl();
        if (finalUrl == null || finalUrl.isBlank()) {
            //요청에서 finalUrl을 안 주면 캠페인의 landingUrl 사용
            finalUrl = campaign.getLandingUrl();
        }
        // slug 저장 충돌 재시도는 “save 위치”에 넣는다
        int maxRetry = 5;
        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            String slug = randomAlphaNumeric(8);

            TrackingLink trackingLink = TrackingLink.builder()
                    .campaign(campaign)
                    .creator(creator)
                    .channel(channel)
                    .slug(slug)
                    .finalUrl(finalUrl)
                    .createdAt(LocalDateTime.now())
                    .status(Status.ACTIVE)
                    .build();

            try {
                // saveAndFlush로 유니크 충돌을 여기서 바로 터뜨려 catch 가능하게
                TrackingLink saved = trackingLinkRepository.saveAndFlush(trackingLink);

                return TrackingLinkResponse.from(saved);

            } catch (DataIntegrityViolationException e) {
                // slug UNIQUE 충돌이면 retry, 그 외는 그대로 던지기
                if (isDuplicateSlug(e)) {
                    if (attempt == maxRetry) {
                        throw new IllegalStateException("slug 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
                    }
                    continue;
                }
                throw e;
            }
        }

        throw new IllegalStateException("slug 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");

    }

    private boolean isDuplicateSlug(DataIntegrityViolationException e) {
        // DB/드라이버별 메시지가 달라서 “느슨하게” 체크하는 편이 현실적
        String msg = (e.getMostSpecificCause() != null) ? e.getMostSpecificCause().getMessage() : e.getMessage();
        if (msg == null) return false;
        msg = msg.toLowerCase();
        return msg.contains("duplicate") && (msg.contains("slug") || msg.contains("idx_tracking_links_slug"));
    }


    public String handleClick(String slug, HttpServletRequest request) {
        // ACTIVE 인 slug 만 허용
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
                .map(TrackingLinkResponse::from)
                .toList();
    }


    //slug 생성기 (간단 버전)
    private String randomAlphaNumeric(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
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
