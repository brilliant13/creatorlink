package com.jung.creatorlink.repository.tracking;

import com.jung.creatorlink.domain.tracking.ClickLog;
import com.jung.creatorlink.domain.tracking.TrackingLink;
import com.jung.creatorlink.dto.stats.CampaignStatsResponse;
import com.jung.creatorlink.dto.stats.CreatorStatsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {

    @Query("""
        select new com.jung.creatorlink.dto.stats.CreatorStatsResponse(
            c.id,
            c.name,
            count(cl.id)
        )
        from Creator c
            left join TrackingLink tl
                on tl.creator = c
               and tl.status = com.jung.creatorlink.domain.common.Status.ACTIVE
            left join ClickLog cl
                on cl.trackingLink = tl
        where c.advertiser.id = :advertiserId
          and c.status = com.jung.creatorlink.domain.common.Status.ACTIVE
        group by c.id, c.name
        order by count(cl.id) desc
        """)
    List<CreatorStatsResponse> findCreatorStatsByAdvertiserId(@Param("advertiserId") Long advertiserId);

    @Query("""
        select new com.jung.creatorlink.dto.stats.CampaignStatsResponse(
            camp.id,
            camp.name,
            count(cl.id)
        )
        from Campaign camp
            left join TrackingLink tl
                on tl.campaign = camp
               and tl.status = com.jung.creatorlink.domain.common.Status.ACTIVE
            left join ClickLog cl
                on cl.trackingLink = tl
        where camp.advertiser.id = :advertiserId
          and camp.status = com.jung.creatorlink.domain.common.Status.ACTIVE
        group by camp.id, camp.name
        order by count(cl.id) desc
        """)
    List<CampaignStatsResponse> findCampaignStatsByAdvertiserId(@Param("advertiserId") Long advertiserId);
}

