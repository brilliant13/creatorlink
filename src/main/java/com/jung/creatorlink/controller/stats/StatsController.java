package com.jung.creatorlink.controller.stats;

import com.jung.creatorlink.dto.stats.CreatorStatsResponse;
import com.jung.creatorlink.service.stats.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "StatsController", description = "통계/대시보드용 API")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/creators")
    @Operation(summary = "크리에이터별 클릭 통계",
            description = "광고주(advertiserId) 기준으로 크리에이터별 총 클릭 수를 조회한다.")
    public List<CreatorStatsResponse> getCreatorStats(@RequestParam Long advertiserId) {
        //지금은 인증이 없어서 advertiserId를 쿼리 파라미터로 받는다.
        //나중에 JWT 붙이면 @RequestParam 제거하고
        //SecurityContext에서 현재 로그인한 광고주의 id를 가져오자.
        return statsService.getCreatorStats(advertiserId);
    }
}
