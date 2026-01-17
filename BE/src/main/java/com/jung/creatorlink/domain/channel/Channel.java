package com.jung.creatorlink.domain.channel;

import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "channels",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_channels_adv_platform_placement",
                        columnNames = {"advertiser_id", "platform", "placement"}
                )
        },
        indexes = {
                @Index(name = "idx_channels_adv_status", columnList = "advertiser_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 광고주 소유
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_id", nullable = false)
    private User advertiser;

    // 플랫폼 (예: INSTAGRAM / YOUTUBE / BLOG 등) - 처음엔 String으로 시작 OK
    @Column(nullable = false, length = 50)
    private String platform;

    // 노출 위치 (예: STORY / BIO / DESCRIPTION / POST 등)
    @Column(nullable = false, length = 100)
    private String placement;

    //  ERD: display_name (NOT NULL)
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    //  ERD: icon_url (NULL)
    @Column(name = "icon_url", length = 1000)
    private String iconUrl;

    @Column(length = 255)
    private String note; // (선택) 채널 설명/메모

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    public void deactivate() {
        this.status = Status.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }

//    public void update(String platform, String placement, String note) {
//        this.platform = platform;
//        this.placement = placement;
//        this.note = note;
//        this.updatedAt = LocalDateTime.now();
//    }

    public void update(String platform, String placement, String displayName, String iconUrl, String note) {
        this.platform = platform;
        this.placement = placement;
        this.displayName = (displayName == null || displayName.isBlank())
                ? defaultDisplayName(platform, placement)
                : displayName;

        this.iconUrl = iconUrl;
        this.note = note;
        this.updatedAt = LocalDateTime.now();
    }
    public static String defaultDisplayName(String platform, String placement) {
        return platform + " > " + placement;
    }

}