package com.jung.creatorlink.domain.tracking;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "click_logs",
        indexes = {
                @Index(name = "idx_click_logs_tracking_link_id", columnList = "tracking_link_id"),
                @Index(name = "idx_click_logs_clicked_at", columnList = "clicked_at")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //어떤 트래킹 링크가 눌렸는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_link_id", nullable = false)
    private TrackingLink trackingLink;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @Column(length = 45)
    private String ip;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 1000)
    private String referer;
}
