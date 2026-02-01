package com.jung.creatorlink.loadTest;

import com.jung.creatorlink.domain.campaign.Campaign;
import com.jung.creatorlink.domain.channel.Channel;
import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.creator.Creator;
import com.jung.creatorlink.domain.tracking.TrackingLink;
import com.jung.creatorlink.domain.user.User;
import com.jung.creatorlink.repository.campaign.CampaignRepository;
import com.jung.creatorlink.repository.channel.ChannelRepository;
import com.jung.creatorlink.repository.creator.CreatorRepository;
import com.jung.creatorlink.repository.tracking.ClickLogRepository;
import com.jung.creatorlink.repository.tracking.TrackingLinkRepository;
import com.jung.creatorlink.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestDataService {
    private final JdbcTemplate jdbcTemplate;

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final CreatorRepository creatorRepository;
    private final ChannelRepository channelRepository;
    private final TrackingLinkRepository trackingLinkRepository;

    private final ClickLogRepository clickLogRepository; // 있으면 좋고, 없으면 reset에서 jdbc로만 지워도 됨

    private final SecureRandom random = new SecureRandom();
    private static final String ALPHANUM = "abcdefghijklmnopqrstuvwxyz0123456789";

    @Transactional
    public void resetAll() {
        // FK 때문에 순서 중요
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE click_logs");
        jdbcTemplate.execute("TRUNCATE TABLE tracking_links");
        jdbcTemplate.execute("TRUNCATE TABLE campaigns");
        jdbcTemplate.execute("TRUNCATE TABLE channels");
        jdbcTemplate.execute("TRUNCATE TABLE creators");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }

    @Transactional
    public SeedResult seed(SeedRequest req) {
        LocalDateTime now = LocalDateTime.now();

        // 1) user(=advertiser) 생성
        User user = userRepository.findByEmail(req.getUserEmail())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(req.getUserEmail())
                                .name(req.getUserName())
                                .passwordHash(req.getPasswordHash())
                                .createdAt(now)  // ← 추가!
                                .build()
                ));

        // 2) campaign 생성 (advertiser_id = user.id)
        List<Campaign> campaigns = new ArrayList<>();
        for (int i = 0; i < req.getCampaigns(); i++) {
            campaigns.add(Campaign.builder()
                    .advertiser(user)
                    .name("Campaign " + (i + 1))
                    .landingUrl(req.getLandingUrl())
                    .description("seed")
                    .startDate(LocalDate.now().minusDays(30))
                    .endDate(LocalDate.now().plusDays(30))
                    .status(Status.ACTIVE)
                    .createdAt(now)    // ← 추가!
                    .updatedAt(now)    // ← 추가!
                    .build());
        }
        campaigns = campaignRepository.saveAll(campaigns);

        // 이번 실험은 “캠페인 1개 기준 UC-10”이 편하니까 첫 번째만 사용
        Campaign campaign = campaigns.get(0);

        // 3) creators 생성
        List<Creator> creators = new ArrayList<>();
        for (int i = 0; i < req.getCreators(); i++) {
            creators.add(Creator.builder()
                    .advertiser(user)
                    .name("Creator " + (i + 1))
                    .channelName("ChannelName " + (i + 1))
                    .channelUrl("https://example.com/creator/" + (i + 1))
                    .note("seed")
                    .status(Status.ACTIVE)
                    .createdAt(now)    // ← 추가!
                    .updatedAt(now)    // ← 추가!
                    .build());
        }
        creators = creatorRepository.saveAll(creators);

        // 4) channels 생성 (platform+placement)
        List<Channel> channels = new ArrayList<>();
        String[] platforms = {"Instagram", "YouTube", "Blog", "TikTok", "X"};
        String[] placements = {"Story", "Feed", "Description", "Body", "Bio"};

        for (int i = 0; i < req.getChannels(); i++) {
            String platform = platforms[i % platforms.length];
            String placement = placements[i % placements.length];
            channels.add(Channel.builder()
                    .advertiser(user)
                    .platform(platform)
                    .placement(placement)
                    .displayName(platform + " + " + placement)
                    .note("seed")
                    .status(Status.ACTIVE)
                    .createdAt(now)    // ← 추가!
                    .updatedAt(now)    // ← 추가!
                    .build());
        }
        channels = channelRepository.saveAll(channels);

        // 5) tracking_links 대량 생성 (creator당 linksPerCreator)
//        int totalLinks = req.getCreators() * req.getLinksPerCreator();
        int totalLinks = creators.size() * req.getLinksPerCreator();

        List<TrackingLink> buffer = new ArrayList<>(5000);

        for (Creator c : creators) {
            for (int j = 0; j < req.getLinksPerCreator(); j++) {
                Channel ch = channels.get(random.nextInt(channels.size()));
                TrackingLink tl = TrackingLink.builder()
                        .campaign(campaign)
                        .creator(c)
                        .channel(ch)
                        .finalUrl(req.getLandingUrl())
                        .status(Status.ACTIVE)
//                        .slug(generateUniqueSlugWithRetry())
                        .slug(generateSlug())
                        .createdAt(now)  // ← 추가!
                        .build();
                buffer.add(tl);

                // 배치 flush (JPA saveAll chunk)
                if (buffer.size() >= 5000) {
                    trackingLinkRepository.saveAll(buffer);
                    buffer.clear();
                }
            }
        }
        if (!buffer.isEmpty()) {
            trackingLinkRepository.saveAll(buffer);
        }

        return new SeedResult(
                user.getId(),
                campaigns.size(),
                creators.size(),
                channels.size(),
                totalLinks
        );
    }

    @Transactional(readOnly = true)
    public List<String> getActiveSlugs(int limit) {
        return trackingLinkRepository.findActiveSlugs(limit);
    }

    private String generateUniqueSlugWithRetry() {
        // seed 전용: 충돌나면 재시도하면 됨
        for (int i = 0; i < 10; i++) {
            String slug = randomSlug(10);
            try {
                // UNIQUE(slug)는 DB가 보장. 여기서는 save 시점에 터질 수 있음.
                return slug;
            } catch (DataIntegrityViolationException ignored) {}
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String generateSlug() {
        // 충돌 확률을 사실상 0으로 만들기 위해 길이를 12로
        return randomSlug(12);
    }

    private String randomSlug(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
