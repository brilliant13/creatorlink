package com.jung.creatorlink.service.tracking;

import com.jung.creatorlink.domain.campaign.Campaign;
import com.jung.creatorlink.domain.creator.Creator;
import com.jung.creatorlink.domain.tracking.ClickLog;
import com.jung.creatorlink.domain.tracking.TrackingLink;
import com.jung.creatorlink.dto.tracking.TrackingLinkCreateRequest;
import com.jung.creatorlink.dto.tracking.TrackingLinkResponse;
import com.jung.creatorlink.repository.campaign.CampaignRepository;
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

    //1) 트래킹 링크 생성
    public TrackingLinkResponse createTrackingLink(TrackingLinkCreateRequest request) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("캠패인을 찾을 수 없습니다."));

        Creator creator = creatorRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("크리에이터를 찾을 수 없습니다."));

        //(선택) 캠페인/크리에이터의 advertiser가 같은지 검증해도 된다.

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
                .slug(slug)
                .finalUrl(finalUrl)
                .createdAt(LocalDateTime.now())
                .build();

        TrackingLink saved = trackingLinkRepository.save(trackingLink);

        return TrackingLinkResponse.builder()
                .id(saved.getId())
                .campaignId(saved.getCampaign().getId())
                .creatorId(saved.getCreator().getId())
                .slug(saved.getSlug())
                .finalUrl(saved.getFinalUrl())
                .build();
    }


    //2) 클릭 처리 + 로그 기록 + 최종 URL 반환
    public String handleClick(String slug, HttpServletRequest request) {

        TrackingLink trackingLink = trackingLinkRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 링크입니다."));

        //클릭 로그 저장 ( 동기 버전)
        ClickLog log = ClickLog.builder()
                .trackingLink(trackingLink)
                .clickedAt(LocalDateTime.now())
                .ip(extractClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .referer(request.getHeader("Referer"))
                .build();

        clickLogRepository.save(log);

        // 나중에는 여기에서 큐로 넣고 비동기로 저장하는 버전도 실험 가능하다.

        return trackingLink.getFinalUrl();
    }

    // 캠페인별 트래킹 링크 목록 조회
    public List<TrackingLinkResponse> getTrackingLinksByCampaign(Long campaignId) {
        List<TrackingLink> links = trackingLinkRepository.findAllByCampaignId(campaignId);

        return links.stream()
                .map(link -> TrackingLinkResponse.builder()
                        .id(link.getId())
                        .campaignId(link.getCampaign().getId())
                        .creatorId(link.getCreator().getId())
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
}
