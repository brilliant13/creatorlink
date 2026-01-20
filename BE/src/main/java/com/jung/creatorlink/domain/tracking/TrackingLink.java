package com.jung.creatorlink.domain.tracking;

import com.jung.creatorlink.domain.campaign.Campaign;
import com.jung.creatorlink.domain.channel.Channel;
import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.creator.Creator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tracking_links",
        indexes = {
                // /t/{slug} 1건 조회
                @Index(name = "idx_tracking_links_slug", columnList = "slug", unique = true),

                // 정책/통계 경로: (FK + status)
                // Soft delete 기본 필터 + 조회/정책/통계용
                @Index(name = "idx_tracking_links_campaign_status", columnList = "campaign_id, status"),
                @Index(name = "idx_tracking_links_creator_status", columnList = "creator_id, status"),
                @Index(name = "idx_tracking_links_channel_status", columnList = "channel_id, status")
        }
//        uniqueConstraints = {
//                // 같은 조합 중복 생성 방지 (Soft delete 재발급 고려: status 포함)
//                @UniqueConstraint(
//                        name = "uk_links_campaign_creator_channel_status",
//                        columnNames = {"campaign_id", "creator_id", "channel_id", "status"}
//                )
//        }
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    // /t/{slug} 에서 사용할 고유 코드
    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    //실제로 리다이렉트할 최종 URL
    @Column(nullable = false, length = 1000)
    private String finalUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // (선택) 운영/추적 위해 updatedAt 두는 편이 보통 더 좋음
    // @Column(nullable = false)
    // private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    public void deactivate() {
        this.status = Status.INACTIVE;
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }
}
