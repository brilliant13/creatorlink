package com.jung.creatorlink.service.stats;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jung.creatorlink.common.exception.ResourceNotFoundException;
import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.dto.stats.*;
import com.jung.creatorlink.repository.campaign.CampaignRepository;
import com.jung.creatorlink.repository.tracking.ClickLogRepository;
import com.jung.creatorlink.repository.tracking.TrackingLinkRepository;
import com.jung.creatorlink.service.cache.StatsCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final ClickLogRepository clickLogRepository;
    private final CampaignRepository campaignRepository;
    private final TrackingLinkRepository trackingLinkRepository;

    private static final TypeReference<List<CombinationStatsResponse>> COMB_LIST =
            new TypeReference<>() {};

    private static final TypeReference<List<ChannelRankingResponse>> RANK_LIST =
            new TypeReference<>() {};

    private final StatsCacheService statsCacheService;


    // KPI (캠페인 성과 탭 상단 카드)
    public CampaignKpiResponse getCampaignKpi(Long campaignId, Long advertiserId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        validateCampaignOwnership(campaignId, advertiserId);

        LocalDate today = LocalDate.now(KST);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toEndExclusive = to.plusDays(1).atStartOfDay();

        CampaignKpiClicksAgg clicksAgg = clickLogRepository.findCampaignKpiClicks(
                campaignId,
                Status.ACTIVE,
                todayStart,
                tomorrowStart,
                fromStart,
                toEndExclusive
        );

        long activeLinks = trackingLinkRepository.countByCampaign_IdAndStatus(campaignId, Status.ACTIVE);

        return new CampaignKpiResponse(
                clicksAgg.getTodayClicks(),
                clicksAgg.getRangeClicks(),
                clicksAgg.getTotalClicks(),
                activeLinks
        );
    }

    // UC-10-1: 조합별 성과 비교
    // 캐시적용
    public List<CombinationStatsResponse> getCombinationStats(Long campaignId, Long advertiserId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        validateCampaignOwnership(campaignId, advertiserId);

        String key = buildCombinationKey(campaignId, from, to);

        return statsCacheService
                .get(key, COMB_LIST)
                .orElseGet(() -> { // 캐시 미스(또는 enabled=false) 시 DB 조회
                    List<CombinationStatsResponse> result = queryCombinationFromDB(campaignId, from, to);
                    statsCacheService.set(key, result); // enabled=true일 때만 저장
                    return result;
                });
    }
//    public List<CombinationStatsResponse> getCombinationStats(Long campaignId, Long advertiserId, LocalDate from, LocalDate to) {
//        validateRange(from, to);
//        validateCampaignOwnership(campaignId, advertiserId);
//
//        LocalDate today = LocalDate.now(KST);
//        LocalDateTime todayStart = today.atStartOfDay();
//        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
//
//        LocalDateTime fromStart = from.atStartOfDay();
//        LocalDateTime toEndExclusive = to.plusDays(1).atStartOfDay();
//
//        return clickLogRepository.findCombinationStats(
//                campaignId,
//                Status.ACTIVE,
//                todayStart,
//                tomorrowStart,
//                fromStart,
//                toEndExclusive
//        );
//    }

    // UC-10-2: 채널 랭킹 Top-N
    //캐시 적용
    public List<ChannelRankingResponse> getChannelRanking(Long campaignId, Long advertiserId, LocalDate from, LocalDate to, int limit) {
        validateRange(from, to);
        validateCampaignOwnership(campaignId, advertiserId);

        int safeLimit = clampLimit(limit);
        String key = buildChannelRankingKey(campaignId, from, to, safeLimit);

        return statsCacheService
                .get(key, RANK_LIST)
                .orElseGet(() -> {
                    List<ChannelRankingResponse> result = queryChannelRankingFromDB(campaignId, from, to, safeLimit);
                    statsCacheService.set(key, result);
                    return result;
                });
    }

//    public List<ChannelRankingResponse> getChannelRanking(Long campaignId, Long advertiserId, LocalDate from, LocalDate to, int limit) {
//        validateRange(from, to);
//        validateCampaignOwnership(campaignId, advertiserId);
//
//        int safeLimit = clampLimit(limit);
//
//        LocalDateTime fromStart = from.atStartOfDay();
//        LocalDateTime toEndExclusive = to.plusDays(1).atStartOfDay();
//
//        return clickLogRepository.findChannelRanking(
//                campaignId,
//                Status.ACTIVE,
//                fromStart,
//                toEndExclusive,
//                PageRequest.of(0, safeLimit)
//        );
//    }
// ========== 캐시 키 생성 ==========

    private String buildCombinationKey(Long campaignId, LocalDate from, LocalDate to) {
        return String.format("stats:comb:%d:%s:%s", campaignId, from, to);
    }

    private String buildChannelRankingKey(Long campaignId, LocalDate from, LocalDate to, int limit) {
        return String.format("stats:rank:%d:%s:%s:%d", campaignId, from, to, limit);
    }

// ========== DB 집계 로직 (기존 로직 분리) ==========

    private List<CombinationStatsResponse> queryCombinationFromDB(Long campaignId, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toEndExclusive = to.plusDays(1).atStartOfDay();

        return clickLogRepository.findCombinationStats(
                campaignId,
                Status.ACTIVE,
                todayStart,
                tomorrowStart,
                fromStart,
                toEndExclusive
        );
    }

    private List<ChannelRankingResponse> queryChannelRankingFromDB(Long campaignId, LocalDate from, LocalDate to, int limit) {
        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toEndExclusive = to.plusDays(1).atStartOfDay();

        return clickLogRepository.findChannelRanking(
                campaignId,
                Status.ACTIVE,
                fromStart,
                toEndExclusive,
                PageRequest.of(0, limit)
        );
    }

    private void validateCampaignOwnership(Long campaignId, Long advertiserId) {
        boolean ok = campaignRepository.existsByIdAndAdvertiser_IdAndStatus(campaignId, advertiserId, Status.ACTIVE);
        if (!ok) {
            // “삭제/비활성은 안 보인다” 정책: 404로 숨김
            throw new ResourceNotFoundException("캠페인을 찾을 수 없습니다.");
        }
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to는 필수입니다.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from은 to보다 늦을 수 없습니다.");
        }
    }

    private int clampLimit(int limit) {
        if (limit <= 0) return 10;
        return Math.min(limit, 50);
    }

    // 광고주별 크리에이터 클릭 통계
    public List<CreatorStatsResponse> getCreatorStats(Long advertiserId) {
//        return clickLogRepository.findCreatorStatsByAdvertiserId(advertiserId);
        var today = LocalDate.now();
        var startOfDay = today.atStartOfDay();
        var endOfDay = today.plusDays(1).atStartOfDay(); // 다음날 0시 직전까지

        return clickLogRepository.findCreatorStatsByAdvertiserId(
                advertiserId,
                startOfDay,
                endOfDay
        );
    }

    // 광고주별 캠페인 클릭 통계
    public List<CampaignStatsResponse> getCampaignStats(Long advertiserId) {
//        return clickLogRepository.findCampaignStatsByAdvertiserId(advertiserId);
        var today = LocalDate.now();
        var startOfDay = today.atStartOfDay();
        var endOfDay = today.plusDays(1).atStartOfDay();

        return clickLogRepository.findCampaignStatsByAdvertiserId(
                advertiserId,
                startOfDay,
                endOfDay
        );
    }

    public TodayStatsResponse getTodayStats(Long advertiserId) {
        LocalDate today = LocalDate.now(); // 필요하면 ZoneId.of("Asia/Seoul")로 조정 가능
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        Long count = clickLogRepository.countTodayClicks(advertiserId, startOfDay, startOfTomorrow);
        if (count == null) {
            count = 0L;
        }

        return new TodayStatsResponse(count);
    }
}
