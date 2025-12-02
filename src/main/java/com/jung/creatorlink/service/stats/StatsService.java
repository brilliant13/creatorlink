package com.jung.creatorlink.service.stats;

import com.jung.creatorlink.dto.stats.CreatorStatsResponse;
import com.jung.creatorlink.repository.tracking.ClickLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final ClickLogRepository clickLogRepository;

    // 광고주별 크리에이터 클릭 통계
    public List<CreatorStatsResponse> getCreatorStats(Long advertiserId) {
        return clickLogRepository.findCreatorStatsByAdvertiserId(advertiserId);
    }
}
