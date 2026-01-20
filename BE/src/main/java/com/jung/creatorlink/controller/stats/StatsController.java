package com.jung.creatorlink.controller.stats;

import com.jung.creatorlink.dto.stats.*;
import com.jung.creatorlink.service.stats.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "StatsController", description = "통계/대시보드용 API")
public class StatsController {

    private final StatsService statsService;
    //KPI
    @GetMapping("/campaigns/{campaignId}/kpi")
    @Operation(summary = "캠페인 KPI (성과 탭 상단 요약)",
            description = "캠페인 범위에서 today/range/total 클릭과 ACTIVE 링크 수를 조회한다.")
    public CampaignKpiResponse getCampaignKpi(
            @PathVariable Long campaignId,
            @RequestParam Long advertiserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return statsService.getCampaignKpi(campaignId, advertiserId, from, to);
    }

    //  UC-10-1
    @GetMapping("/campaigns/{campaignId}/combinations")
    @Operation(summary = "조합별 성과 비교 (Creator x Channel)",
            description = "캠페인 범위에서 크리에이터×채널 조합별 today/range/total 클릭을 조회한다. 클릭이 없어도 0으로 반환한다.")
    public List<CombinationStatsResponse> getCombinationStats(
            @PathVariable Long campaignId,
            @RequestParam Long advertiserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return statsService.getCombinationStats(campaignId, advertiserId, from, to);
    }

    //  UC-10-2
    @GetMapping("/campaigns/{campaignId}/channels/ranking")
    @Operation(summary = "채널 랭킹 (Top-N)",
            description = "캠페인 범위에서 기간(from~to) 내 채널별 클릭 수 Top-N을 조회한다. 클릭이 있는 채널 중심으로 반환한다.")
    public List<ChannelRankingResponse> getChannelRanking(
            @PathVariable Long campaignId,
            @RequestParam Long advertiserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return statsService.getChannelRanking(campaignId, advertiserId, from, to, limit);
    }

    @GetMapping("/creators")
    @Operation(summary = "크리에이터별 클릭 통계",
            description = "광고주(advertiserId) 기준으로 크리에이터별 총 클릭 수를 조회한다.")
    public List<CreatorStatsResponse> getCreatorStats(@RequestParam Long advertiserId) {
        //지금은 인증이 없어서 advertiserId를 쿼리 파라미터로 받는다.
        //나중에 JWT 붙이면 @RequestParam 제거하고
        //SecurityContext에서 현재 로그인한 광고주의 id를 가져오자.
        return statsService.getCreatorStats(advertiserId);
    }

    @GetMapping("/campaigns")
    @Operation(summary = "캠페인별 클릭 통계",
            description = "광고주(advertiserId) 기준으로 캠페인별 총 클릭 수를 조회한다.")
    public List<CampaignStatsResponse> getCampaignStats(@RequestParam Long advertiserId) {
        return statsService.getCampaignStats(advertiserId);
    }

    @GetMapping("/today")
    @Operation(summary = "오늘 클릭 통계", description = "광고주(advertiserId) 기준 오늘 발생한 총 클릭 수를 조회한다.")
    public TodayStatsResponse getTodayStats(@RequestParam Long advertiserId) {
        return statsService.getTodayStats(advertiserId);
    }
}
