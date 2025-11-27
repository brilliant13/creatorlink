package com.jung.creatorlink.repository.campaign;

import com.jung.creatorlink.domain.campaign.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    //특정 광고주가 가진 캠페인 목록
    List<Campaign> findByAdvertiserId(Long advertiserId);
}
