package com.jung.creatorlink.loadTest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "부하테스트", description = "부하테스트용 테스트 데이터 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestDataController {

    private final TestDataService testDataService;
    private final TestTokenGuard tokenGuard;

    // 1) 전체 초기화
    @PostMapping("/reset")
    @Operation(
            summary = "테스트 데이터 전체 초기화",
            description = "부하테스트를 위해 생성된 모든 테스트 데이터를 삭제한다. 운영 환경에서는 비활성화되어야 한다."
    )
    //@Operation(summary = "트랙킹 링크 발급", description = "캠페인 + 크리에이터 조합으로 트래킹 링크를 발급한다.")
    public ResponseEntity<?> reset(@RequestHeader("X-TEST-TOKEN") String token) {
        tokenGuard.check(token);
        testDataService.resetAll();
        return ResponseEntity.ok("OK");
    }

    // 2) 시드 생성
    @PostMapping("/seed")
    @Operation(
            summary = "테스트 데이터 생성",
            description = "부하테스트를 위한 시드 데이터(캠페인, 크리에이터, 트래킹 링크 등)를 생성한다."
    )
    public ResponseEntity<?> seed(
            @RequestHeader("X-TEST-TOKEN") String token,
            @RequestBody SeedRequest req
    ) {
        tokenGuard.check(token);
        SeedResult result = testDataService.seed(req);
        return ResponseEntity.ok(result);
    }

    // 3) slug 리스트 제공 (k6 setup에서 가져갈 용도)
    @GetMapping("/slugs")
    @Operation(
            summary = "활성 트래킹 링크 slug 목록 조회",
            description = "k6 부하테스트 스크립트의 setup 단계에서 사용할 활성 상태의 트래킹 링크 slug 목록을 반환한다."
    )
    public ResponseEntity<?> slugs(
            @RequestHeader("X-TEST-TOKEN") String token,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        tokenGuard.check(token);
        List<String> slugs = testDataService.getActiveSlugs(limit);
        return ResponseEntity.ok(slugs);
    }
}
