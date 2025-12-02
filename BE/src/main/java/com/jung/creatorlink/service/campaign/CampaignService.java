package com.jung.creatorlink.service.campaign;


import com.jung.creatorlink.domain.campaign.Campaign;
import com.jung.creatorlink.domain.user.User;
import com.jung.creatorlink.dto.campaign.CampaignCreateRequest;
import com.jung.creatorlink.dto.campaign.CampaignResponse;
import com.jung.creatorlink.repository.campaign.CampaignRepository;
import com.jung.creatorlink.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
//비즈니스 로직. 트랜잭션을 시작한다.
//서비스 레이어에서는 도메인 모델을 활용해 애플리케이션에서 제공하는 핵심 기능을 제공한다.

public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;


    public CampaignResponse createCampaign(CampaignCreateRequest request) {
        // 비즈니스 로직 (검증, 엔티티 생성, 저장 등)
        //이쪽은 HTTP를 전혀 몰라. 우리 도메인 규칙대로 캠페인을 하나 만드는 일
        //1. 광고주 존재 여부 확인
        User advertiser = userRepository.findById(request.getAdvertiserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 광고주입니다."));

        //2. Campaign 엔터티 생성
        Campaign campaign = Campaign.builder()
                .advertiser(advertiser) //= 여기서 User 엔티티 설정
                .name(request.getName())
                .description(request.getDescription())
                .landingUrl(request.getLandingUrl())
                .startDate((request.getStartDate()))
                .endDate((request.getEndDate()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        //3. 저장
        Campaign saved = campaignRepository.save(campaign);

        //4. 응답 DTO로 변환
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaignsByAdvertiser(Long advertiserId) {
        List<Campaign> campaigns = campaignRepository.findByAdvertiserId(advertiserId);
        //List<Campaign> -> Stream<Campaign> -> (toResponse 메소드 거치고) -> List<Campaign>
        //스트림객체에서 스트림객체로. 매핑. map()
        return campaigns.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaign(Long campaignId, Long advertiserId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("캠패인을 찾을 수 없습니다."));

        if (!campaign.getAdvertiser().getId().equals(advertiserId)) {
            throw new IllegalArgumentException("해당 광고주의 캠페인이 아닙니다.");
        }
        return toResponse(campaign);
    }


    private CampaignResponse toResponse(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .advertiserId(campaign.getAdvertiser().getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .landingUrl(campaign.getLandingUrl())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .build();
    }
}
