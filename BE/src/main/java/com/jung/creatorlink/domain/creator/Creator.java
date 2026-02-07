package com.jung.creatorlink.domain.creator;

import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "creators",
        indexes = {
                @Index(name = "idx_creators_adv_status", columnList = "advertiser_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Creator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //광고주(User) - 여러 Creator가 한 User에 속함 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_id", nullable = false)
    private User advertiser;

    @Column(nullable = false, length = 100)
    private String name;          // 크리에이터/담당자 이름

    @Column(nullable = false, length = 150)
    private String channelName;   // 채널명

    @Column(nullable = false, length = 255)
    private String channelUrl;    // 채널 URL

    @Column(length = 500)
    private String note;          // 메모

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;


    public void update(String name, String channelName, String channelUrl, String note) {
        this.name = name;
        this.channelName = channelName;
        this.channelUrl = channelUrl;
        this.note = note;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }
}
