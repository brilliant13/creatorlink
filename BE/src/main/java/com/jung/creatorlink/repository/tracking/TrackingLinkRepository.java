package com.jung.creatorlink.repository.tracking;

import com.jung.creatorlink.domain.tracking.TrackingLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingLinkRepository extends JpaRepository<TrackingLink, Long> {

    Optional<TrackingLink> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // 캠페인 ID 기준 모든 트래킹 링크 조회
    List<TrackingLink> findAllByCampaignId(Long campaignId);

    boolean existsByCampaign_Id(Long campaignId);

    boolean existsByCreator_Id(Long creatorId);

}
