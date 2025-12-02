package com.jung.creatorlink.controller.creator;

import com.jung.creatorlink.dto.creator.CreatorCreateRequest;
import com.jung.creatorlink.dto.creator.CreatorResponse;
import com.jung.creatorlink.service.creator.CreatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/creators")
@RequiredArgsConstructor
@Tag(name = "CreatorController", description = "크리에이터 생성 및 조회 API")

public class CreatorController {

    private final CreatorService creatorService;

    //크리에이터 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "크리에이터 생성", description = "")
    public CreatorResponse createCreator(@Valid @RequestBody CreatorCreateRequest request) {
        return creatorService.createCreator(request);
    }

    //광고주별 크리에이터 목록 조회
    @GetMapping
    @Operation(summary = "광고주별 크리에이터 목록 조회", description = "")
    public List<CreatorResponse> getCreatorsByAdvertiser(@RequestParam Long advertiserId) {
        return creatorService.getCreatorsByAdvertiser(advertiserId);
    }

    //특정 크리에이터 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "특정 크리에이터 단건 조회", description = "")
    public CreatorResponse getCreator(@PathVariable Long id, @RequestParam Long advertiserId) {
        // 지금은 인증이 없어서 advertiserId를 쿼리 파라미터로 받아서
        // "해당 광고주의 Creator인지" 검사.
        // 나중에 JWT 붙이면 advertiserId는 안 받고,
        // 현재 로그인 유저 ID를 Security에서 꺼내서 쓸 예정.
        return creatorService.getCreator(id, advertiserId);
    }
}
