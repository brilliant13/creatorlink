package com.jung.creatorlink.service.stats;

import com.jung.creatorlink.dto.stats.CampaignStatsResponse;
import com.jung.creatorlink.dto.stats.CreatorStatsResponse;
import com.jung.creatorlink.dto.stats.TodayStatsResponse;
import com.jung.creatorlink.repository.tracking.ClickLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final ClickLogRepository clickLogRepository;

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
