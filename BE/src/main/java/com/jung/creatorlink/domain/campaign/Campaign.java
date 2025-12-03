package com.jung.creatorlink.domain.campaign;

import com.jung.creatorlink.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //광고주 (User)
    //Adversiter가 User라서, User엔터티랑 ManyToOne관계를 맺었다는 게 포인트.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_id", nullable = false)
    private User advertiser; // ← 이게 User와의 FK 관계

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    //기본 랜딩 URL (링크 클릭 시 기본적으로 가는 주소)
    @Column(nullable = false, length = 500)
    private String landingUrl;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //나중에 수정할 때 쓸 수 있도록 헬퍼 메서드 하나 정도
    public void update(String name, String description, String landingUrl, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.description = description;
        this.landingUrl = landingUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }

}
