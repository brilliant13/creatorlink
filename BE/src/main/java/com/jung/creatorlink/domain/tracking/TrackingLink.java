package com.jung.creatorlink.domain.tracking;

import com.jung.creatorlink.domain.campaign.Campaign;
import com.jung.creatorlink.domain.creator.Creator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tracking_links",
        indexes = {
                @Index(name = "idx_tracking_links_slug", columnList = "slug", unique = true),
                @Index(name = "idx_tracking_links_campaign_id", columnList = "campaign_id"),
                @Index(name = "idx_tracking_links_creator_id", columnList = "creator_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TrackingLink {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        //어떤 캠페인에 속한 링크인지
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "campaign_id", nullable = false)
        private Campaign campaign;

        //어떤 크리에이터에게 준 링크인지
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "creator_id", nullable = false)
        private Creator creator;

        // /t/{slug} 에서 사용할 고유 코드
        @Column(nullable = false, unique = true, length = 50)
        private String slug;

        //실제로 리다이렉트할 최종 URL
        @Column(nullable = false, length = 1000)
        private String finalUrl;

        @Column(nullable = false)
        private LocalDateTime createdAt;
}
