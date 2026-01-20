package com.jung.creatorlink.repository.tracking;

import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.tracking.ClickLog;
import com.jung.creatorlink.domain.tracking.TrackingLink;
import com.jung.creatorlink.dto.stats.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {

    // UC-10-1: 조합별 성과 비교 (0도 포함해야 하므로 TrackingLink 기준 LEFT JOIN)
    @Query("""
            select new com.jung.creatorlink.dto.stats.CombinationStatsResponse(
                cr.id,
                cr.name,
                ch.id,
                concat(ch.platform, ' ', ch.placement),
                coalesce(sum(case when cl.clickedAt >= :todayStart and cl.clickedAt < :tomorrowStart then 1 else 0 end), 0),
                coalesce(sum(case when cl.clickedAt >= :from and cl.clickedAt < :to then 1 else 0 end), 0),
                count(cl.id)
            )
            from TrackingLink tl
              join tl.creator cr
              join tl.channel ch
              left join ClickLog cl on cl.trackingLink = tl
            where tl.campaign.id = :campaignId
              and tl.status = :active
            group by cr.id, cr.name, ch.id, ch.platform, ch.placement
            order by
              coalesce(sum(case when cl.clickedAt >= :todayStart and cl.clickedAt < :tomorrowStart then 1 else 0 end), 0) desc,
              coalesce(sum(case when cl.clickedAt >= :from and cl.clickedAt < :to then 1 else 0 end), 0) desc,
              cr.id asc, ch.id asc
            """)
    List<CombinationStatsResponse> findCombinationStats(
            @Param("campaignId") Long campaignId,
            @Param("active") Status active,
            @Param("todayStart") LocalDateTime todayStart,
            @Param("tomorrowStart") LocalDateTime tomorrowStart,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // UC-10-2: 채널 랭킹 (클릭 있는 것만 의미 -> ClickLog 기준 JOIN + limit)
    @Query("""
            select new com.jung.creatorlink.dto.stats.ChannelRankingResponse(
                ch.id,
                concat(ch.platform, ' ', ch.placement),
                count(cl.id)
            )
            from ClickLog cl
              join cl.trackingLink tl
              join tl.channel ch
            where tl.campaign.id = :campaignId
              and tl.status = :active
              and cl.clickedAt >= :from and cl.clickedAt < :to
            group by ch.id, ch.platform, ch.placement
            order by count(cl.id) desc, ch.id asc
            """)
    List<ChannelRankingResponse> findChannelRanking(
            @Param("campaignId") Long campaignId,
            @Param("active") Status active,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );


    //    @Query("""
//        select new com.jung.creatorlink.dto.stats.CampaignKpiClicksAgg(
//            coalesce(sum(case when cl.clickedAt >= :todayStart and cl.clickedAt < :tomorrowStart then 1L else 0L end), 0L),
//            coalesce(sum(case when cl.clickedAt >= :from and cl.clickedAt < :to then 1L else 0L end), 0L),
//            count(cl.id)
//        )
//        from ClickLog cl
//          join cl.trackingLink tl
//        where tl.campaign.id = :campaignId
//          and tl.status = :active
//        """)
    @Query("""
                select new com.jung.creatorlink.dto.stats.CampaignKpiClicksAgg(
                    coalesce(sum(case when cl.clickedAt >= :todayStart and cl.clickedAt < :tomorrowStart then 1L else 0L end), 0L),
                    coalesce(sum(case when cl.clickedAt >= :from and cl.clickedAt < :to then 1L else 0L end), 0L),
                    count(cl.id)
                )
                from ClickLog cl
                  join cl.trackingLink tl
                where tl.campaign.id = :campaignId
                  and tl.status = :active
            """)
    CampaignKpiClicksAgg findCampaignKpiClicks(
            @Param("campaignId") Long campaignId,
            @Param("active") Status active,
            @Param("todayStart") LocalDateTime todayStart,
            @Param("tomorrowStart") LocalDateTime tomorrowStart,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );


    @Query("""
            select new com.jung.creatorlink.dto.stats.CreatorStatsResponse(
                c.id,
                c.name,
                count(cl.id),
                sum(case 
                        when cl.clickedAt between :start and :end then 1 
                        else 0 
                    end)
            )
            from Creator c
                left join TrackingLink tl
                    on tl.creator = c 
                left join ClickLog cl
                    on cl.trackingLink = tl
            where c.advertiser.id = :advertiserId
              and c.status = com.jung.creatorlink.domain.common.Status.ACTIVE
            group by c.id, c.name
            order by count(cl.id) desc
            """)
    List<CreatorStatsResponse> findCreatorStatsByAdvertiserId(
            @Param("advertiserId") Long advertiserId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select new com.jung.creatorlink.dto.stats.CampaignStatsResponse(
                camp.id,
                camp.name,
                count(cl.id),
                sum(case 
                        when cl.clickedAt between :start and :end then 1 
                        else 0 
                    end)
            )
            from Campaign camp
                left join TrackingLink tl
                    on tl.campaign = camp
                left join ClickLog cl
                    on cl.trackingLink = tl
            where camp.advertiser.id = :advertiserId
              and camp.status = com.jung.creatorlink.domain.common.Status.ACTIVE
            group by camp.id, camp.name
            order by count(cl.id) desc
            """)
    List<CampaignStatsResponse> findCampaignStatsByAdvertiserId(
            @Param("advertiserId") Long advertiserId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select count(cl.id)
            from ClickLog cl
                join cl.trackingLink tl
                join tl.campaign camp
            where camp.advertiser.id = :advertiserId
              and camp.status = com.jung.creatorlink.domain.common.Status.ACTIVE
              and tl.status = com.jung.creatorlink.domain.common.Status.ACTIVE
              and cl.clickedAt >= :start
              and cl.clickedAt < :end
            """)
    Long countTodayClicks(
            @Param("advertiserId") Long advertiserId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}



