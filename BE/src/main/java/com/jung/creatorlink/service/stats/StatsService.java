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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)
public class StatsService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final ClickLogRepository clickLogRepository;
    private final CampaignRepository campaignRepository;
    private final TrackingLinkRepository trackingLinkRepository;
    //Stampede 방지용 키별 락 저장소
    //같은 캐시 키에 대해 동시 요청이 오면 1개만 DB 조회하도록 순차 처리
    private final StatsCacheService statsCacheService;

    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    private static final TypeReference<List<CombinationStatsResponse>> COMB_LIST =
            new TypeReference<>() {
            };

    private static final TypeReference<List<ChannelRankingResponse>> RANK_LIST =
            new TypeReference<>() {
            };


    // ========== 캐시 적용 메서드 (Stampede 방지, public, @Transactional 없음) ====================
    // ========== 캐시 적용 (캐시 HIT 시 DB 접근 안 함) ==========
    // UC-10-1: 조합별 성과 비교
    // Stampede 방지, 락 추가
    public List<CombinationStatsResponse> getCombinationStats(
            Long campaignId, Long advertiserId, LocalDate from, LocalDate to) {
        validateRange(from, to); // ← 모든 스레드가 자유롭게 실행 //OK - 커넥션 불필요

        String key = buildCombinationKey(campaignId, from, to); //검증 전에 키생성

//        validateCampaignOwnership(campaignId, advertiserId); // ← 여기도 자유롭게 실행 //얘가 campaignRepository.existBy()호출하면서 커넥션 획득 시도한다. -> 커넥션 풀(10개) 고갈 ->27% timeout


        //캐시 확인 먼저
        // 1차 캐시 확인 (HIT면 바로 반환, DB 안 감)
        Optional<List<CombinationStatsResponse>> cached = statsCacheService.get(key, COMB_LIST);
        if (cached.isPresent()) return cached.get(); // ← HIT면 여기서 바로 반환, 대기 없음

        // 캐시 MISS → 같은 키에 대해 락 획득 (Stampede 방지)
        // 동일 키 요청이 100개 동시에 오면, 1개만 락 획득하고 나머지 99개는 여기서 대기
        // lock 안 걸면: Cache MISS나고 100개가 동시에 DB 커넥션 풀 요구하는 순간 timeout시간 측정 시작함. HikariCP default 30s 지나면 timeout error 발생.
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {  // ← ★ 여기서 대기 발생
            // 이 블록 안에는 한 번에 1개 스레드만 들어감
            try {
                // 2차 캐시 확인 (Double-check)
                // 대기하는 동안 먼저 들어간 스레드가 캐시를 채웠을 수 있음
                cached = statsCacheService.get(key, COMB_LIST);
                if (cached.isPresent()) return cached.get();

                //여기서 validation(첫 요청만 실행)
                validateCampaignOwnership(campaignId, advertiserId);

                // 첫 번째 스레드만 DB 집계 실행 → 결과를 캐시에 저장
                var result = queryCombinationFromDB(campaignId, from, to); // DB 조회
                statsCacheService.set(key, result);
                return result;
            } finally {
                // 락 해제 → 대기 중이던 스레드들이 깨어나서 2차 확인에서 HIT
                locks.remove(key);
            }
        } // ← 나오면 다음 스레드가 들어감
    }

    // 캐시적용
//    public List<CombinationStatsResponse> getCombinationStats(Long campaignId, Long advertiserId, LocalDate from, LocalDate to) {
//        validateRange(from, to);
//        validateCampaignOwnership(campaignId, advertiserId);
//
//        String key = buildCombinationKey(campaignId, from, to);
//
//        return statsCacheService
//                .get(key, COMB_LIST)
//                .orElseGet(() -> { // 캐시 미스(또는 enabled=false) 시 DB 조회
//                    List<CombinationStatsResponse> result = queryCombinationFromDB(campaignId, from, to); //여기서 커넥션 획득
//                    statsCacheService.set(key, result); // enabled=true일 때만 저장
//                    return result;
//                });
//    }

    //캐시 미적용
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
    // Stampede 방지, 락 추가
    public List<ChannelRankingResponse> getChannelRanking(
            Long campaignId, Long advertiserId, LocalDate from, LocalDate to, int limit) {
        validateRange(from, to);
        validateCampaignOwnership(campaignId, advertiserId);

        int safeLimit = clampLimit(limit);
        String key = buildChannelRankingKey(campaignId, from, to, safeLimit);

        // 1차 캐시 확인
        Optional<List<ChannelRankingResponse>> cached = statsCacheService.get(key, RANK_LIST);
        if (cached.isPresent()) return cached.get();

        // Stampede 방지: 같은 키에 대해 1개 스레드만 DB 조회
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            try {
                // Double-check: 대기 중 다른 스레드가 캐시 채웠는지 확인
                cached = statsCacheService.get(key, RANK_LIST);
                if (cached.isPresent()) return cached.get();

                var result = queryChannelRankingFromDB(campaignId, from, to, safeLimit);
                statsCacheService.set(key, result);
                return result;
            } finally {
                locks.remove(key);
            }
        }
    }

    //캐시 적용
//    public List<ChannelRankingResponse> getChannelRanking(Long campaignId, Long advertiserId, LocalDate from, LocalDate to, int limit) {
//        validateRange(from, to);
//        validateCampaignOwnership(campaignId, advertiserId);
//
//        int safeLimit = clampLimit(limit);
//        String key = buildChannelRankingKey(campaignId, from, to, safeLimit);
//
//        return statsCacheService
//                .get(key, RANK_LIST)
//                .orElseGet(() -> {
//                    List<ChannelRankingResponse> result = queryChannelRankingFromDB(campaignId, from, to, safeLimit);
//                    statsCacheService.set(key, result);
//                    return result;
//                });
//    }
    //캐시 미적용
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


    // ========== 캐시 미적용 메서드 (항상 DB 접근) (public, @Transactional 있음) ==========
    // KPI (캠페인 성과 탭 상단 카드)
    @Transactional(readOnly = true)
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

    // 광고주별 크리에이터 클릭 통계
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    // ========== 내부 DB 조회 메서드 (캐시용, @Transactional 필요) ==========
    // ========== 내부 헬퍼 (실제 DB 쿼리 실행, DB접근) ==========
    @Transactional(readOnly = true)
    List<CombinationStatsResponse> queryCombinationFromDB(Long campaignId, LocalDate from, LocalDate to) {
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

    @Transactional(readOnly = true)
    List<ChannelRankingResponse> queryChannelRankingFromDB(Long campaignId, LocalDate from, LocalDate to, int limit) {
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

    @Transactional(readOnly = true)
    void validateCampaignOwnership(Long campaignId, Long advertiserId) {
        boolean ok = campaignRepository.existsByIdAndAdvertiser_IdAndStatus(campaignId, advertiserId, Status.ACTIVE);
        if (!ok) {
            // “삭제/비활성은 안 보인다” 정책: 404로 숨김
            throw new ResourceNotFoundException("캠페인을 찾을 수 없습니다.");
        }
    }

    // ========== Utility 메서드 (DB 접근 없음) ==========
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

    // ========== 캐시 키 생성 ==========
    private String buildCombinationKey(Long campaignId, LocalDate from, LocalDate to) {
        return String.format("stats:comb:%d:%s:%s", campaignId, from, to);
    }

    private String buildChannelRankingKey(Long campaignId, LocalDate from, LocalDate to, int limit) {
        return String.format("stats:rank:%d:%s:%s:%d", campaignId, from, to, limit);
    }

}
