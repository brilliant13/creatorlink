package com.jung.creatorlink.repository.tracking;

import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.tracking.TrackingLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrackingLinkRepository extends JpaRepository<TrackingLink, Long> {
    // 리다이렉트용: ACTIVE 인 slug 만 유효
    Optional<TrackingLink> findBySlugAndStatus(String slug, Status status);

    boolean existsBySlug(String slug);

    // 캠페인 ID 기준 ACTIVE 링크 목록 조회 (대시보드 등)
    List<TrackingLink> findAllByCampaign_IdAndStatus(Long campaignId, Status status);

    // (필요 시) 크리에이터 ID 기준 ACTIVE 링크 목록 조회
    List<TrackingLink> findAllByCreator_IdAndStatus(Long creatorId, Status status);

    // 이 캠페인에 ACTIVE 링크가 하나라도 남아 있는지?
    boolean existsByCampaign_IdAndStatus(Long campaignId, Status status);

    // 이 크리에이터에 ACTIVE 링크가 하나라도 남아 있는지?
    boolean existsByCreator_IdAndStatus(Long creatorId, Status status);

    boolean existsByCampaign_IdAndCreator_IdAndChannel_IdAndStatus(
            Long campaignId, Long creatorId, Long channelId, Status status
    );

    //KPI(링크 수) + 정합성 강제(일괄 비활성) 추가
    long countByCampaign_IdAndStatus(Long campaignId, Status status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TrackingLink tl
           set tl.status = :inactive
         where tl.creator.id = :creatorId
           and tl.status = :active
    """)
    int deactivateAllByCreatorId(
            @Param("creatorId") Long creatorId,
            @Param("active") Status active,
            @Param("inactive") Status inactive
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TrackingLink tl
           set tl.status = :inactive
         where tl.channel.id = :channelId
           and tl.status = :active
    """)
    int deactivateAllByChannelId(
            @Param("channelId") Long channelId,
            @Param("active") Status active,
            @Param("inactive") Status inactive
    );



}
