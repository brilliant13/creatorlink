package com.jung.creatorlink.controller.campaign;


import com.jung.creatorlink.dto.campaign.CampaignCreateRequest;
import com.jung.creatorlink.dto.campaign.CampaignResponse;
import com.jung.creatorlink.dto.campaign.CampaignUpdateRequest;
import com.jung.creatorlink.service.campaign.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
//@Tag(name = "Campaign", description = "캠페인 생성 및 조회 API")
@Tag(name = "CampaignController", description = "캠페인 생성 및 조회 API")
//클라이언트로부터 요청을 받고 해당 요청에 대해 서비스 레이어에 구현된 적절한 메소드를 호출해서 결괏값을 받는다.
//클라이언트로부터 들어오는 HTTP 요청을 받아서 처리하고, 그에 따른 결과를 HTTP 응답으로 반환하는 역할을 한다.
public class CampaignController {
    // “웹/HTTP 관련 일”만 담당하는 애야.

    private final CampaignService campaignService;

    //캠패인 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "캠페인 생성", description = "광고주의 캠페인을 생성한다.")
    public CampaignResponse createCampaign(@Valid @RequestBody CampaignCreateRequest request) {
        return campaignService.createCampaign(request);
    }

    //광고주별 캠페인 목록 조회
    @GetMapping
    @Operation(summary = "광고주별 캠페인 목록 조회", description = "")
    public List<CampaignResponse> getCampaigns(@RequestParam Long advertiserId) {
        return campaignService.getCampaignsByAdvertiser(advertiserId);
    }

    //특정 캠패인 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "특정 캠패인 단건 조회", description = "")
    public CampaignResponse getCampaign(@PathVariable Long id, @RequestParam Long advertiserId){
        //지금은 인증이 없어서 advertiserId를 쿼리 파라미터로 받는 구조이다.
        //나중에 JWT 붙이면 @RequestParam Long advertiserId 삭제
        //Securit에서 꺼낸 현재 로그인 유저의 id를 쓰게 바꾸면 됨.
        return campaignService.getCampaign(id, advertiserId);
    }

    // 캠페인 수정
    @PutMapping("/{id}")
    @Operation(summary = "캠페인 수정",
            description = "캠페인 기본 정보(이름, 설명, 기간, 랜딩 URL)를 수정한다.")
    public CampaignResponse updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignUpdateRequest request
    ) {
        return campaignService.updateCampaign(id, request);
    }

    // 캠페인 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "캠페인 삭제",
            description = "광고주가 소유한 캠페인을 삭제한다. "
                    + "단, 연결된 트래킹 링크가 있으면 삭제 불가.")
    public void deleteCampaign(
            @PathVariable Long id,
            @RequestParam Long advertiserId
    ) {
        campaignService.deleteCampaign(id, advertiserId);
    }


}
