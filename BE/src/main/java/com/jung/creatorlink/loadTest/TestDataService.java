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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
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
//        if (campaigns.isEmpty()) {
//            throw new IllegalStateException("No campaigns created");
//        }

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
//        List<Channel> channels = new ArrayList<>();
//        String[] platforms = {"Instagram", "YouTube", "Blog", "TikTok", "X"};
//        String[] placements = {"Story", "Feed", "Description", "Body", "Bio"};
//
//        for (int i = 0; i < req.getChannels(); i++) {
//            String platform = platforms[i % platforms.length];
//            String placement = placements[i % placements.length];
//            channels.add(Channel.builder()
//                    .advertiser(user)
//                    .platform(platform)
//                    .placement(placement)
//                    .displayName(platform + " + " + placement)
//                    .note("seed")
//                    .status(Status.ACTIVE)
//                    .createdAt(now)    // ← 추가!
//                    .updatedAt(now)    // ← 추가!
//                    .build());
//        }
//        channels = channelRepository.saveAll(channels);

        // 4) channels 생성 (platform+placement) - 중복 없이 생성
        List<Channel> channels = new ArrayList<>();

        String[] platforms = {"Instagram", "YouTube", "Blog", "TikTok", "X"};
        String[] placements = {"Story", "Feed", "Description", "Body", "Bio"};

// 모든 조합 생성 (최대 25개)
        List<String[]> combos = new ArrayList<>();
        for (String p : platforms) {
            for (String pl : placements) {
                combos.add(new String[]{p, pl});
            }
        }

// 요청 개수 상한 처리 (25 초과면 25로 clamp)
        int channelCount = Math.min(req.getChannels(), combos.size());

        for (int i = 0; i < channelCount; i++) {
            String platform = combos.get(i)[0];
            String placement = combos.get(i)[1];

            channels.add(Channel.builder()
                    .advertiser(user)
                    .platform(platform)
                    .placement(placement)
                    .displayName(platform + " + " + placement)
                    .note("seed")
                    .status(Status.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }

        channels = channelRepository.saveAll(channels);


        // 5) tracking_links 대량 생성 (creator당 linksPerCreator)
//        int totalLinks = req.getCreators() * req.getLinksPerCreator();
//        int totalLinks = creators.size() * req.getLinksPerCreator();
//
//        List<TrackingLink> buffer = new ArrayList<>(5000);
//
//        for (Creator c : creators) {
//            for (int j = 0; j < req.getLinksPerCreator(); j++) {
//                Channel ch = channels.get(random.nextInt(channels.size()));
//                TrackingLink tl = TrackingLink.builder()
//                        .campaign(campaign)
//                        .creator(c)
//                        .channel(ch)
//                        .finalUrl(req.getLandingUrl())
//                        .status(Status.ACTIVE)
////                        .slug(generateUniqueSlugWithRetry())
//                        .slug(generateSlug())
//                        .createdAt(now)  // ← 추가!
//                        .build();
//                buffer.add(tl);
//
//                // 배치 flush (JPA saveAll chunk)
//                if (buffer.size() >= 5000) {
//                    trackingLinkRepository.saveAll(buffer);
//                    buffer.clear();
//                }
//            }
//        }
//        if (!buffer.isEmpty()) {
//            trackingLinkRepository.saveAll(buffer);
//        }

        // 5) tracking_links 대량 생성 (creator당 linksPerCreator)
        // 제약: (campaign_id, creator_id, channel_id, status) UNIQUE
        // => 같은 캠페인에서 같은 creator는 같은 channel로 ACTIVE 링크를 2개 만들 수 없음

        int linksPerCreator = req.getLinksPerCreator();
        int maxLinksPerCreator = channels.size(); // channel 중복 금지면 여기까지가 상한
        int actualLinksPerCreator = Math.min(linksPerCreator, maxLinksPerCreator);

        if (linksPerCreator > maxLinksPerCreator) {
            // 운영에선 로그만 남기거나 예외로 막는 것도 선택
            // throw new IllegalArgumentException("linksPerCreator는 channels 개수 이하로 설정해야 합니다.");
            log.warn("linksPerCreator({}) > channels({}) 이므로 {}로 clamp 합니다.",
                    linksPerCreator, maxLinksPerCreator, actualLinksPerCreator);
        }

        int totalLinks = creators.size() * actualLinksPerCreator;

        List<TrackingLink> buffer = new ArrayList<>(5000);

        int activeCnt = 0;
        int inactiveCnt = 0;

        double inactiveRatio = req.getInactiveLinkRatio(); // 0.0 ~ 0.99
        inactiveRatio = Math.max(0.0, Math.min(0.99, inactiveRatio));

        for (Creator c : creators) {
            // creator마다 채널을 섞어서 "중복 없이" 앞에서 N개만 사용
            List<Channel> shuffled = new ArrayList<>(channels);
            Collections.shuffle(shuffled, random);

            for (int j = 0; j < actualLinksPerCreator; j++) {
                Channel ch = shuffled.get(j);

                //status 섞기 (inactive/active)
                Status status = (random.nextDouble() < inactiveRatio)
                        ? Status.INACTIVE
                        : Status.ACTIVE;

                if (status == Status.ACTIVE) activeCnt++;
                else inactiveCnt++;

                TrackingLink tl = TrackingLink.builder()
                        .campaign(campaign)
                        .creator(c)
                        .channel(ch)
                        .finalUrl(req.getLandingUrl())
//                        .status(Status.ACTIVE)
                        .status(status)
                        .slug(generateSlug())
                        .createdAt(now)
                        .build();

                buffer.add(tl);

                if (buffer.size() >= 5000) {
                    trackingLinkRepository.saveAll(buffer);
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            trackingLinkRepository.saveAll(buffer);
        }

        if (activeCnt < 100) {
            log.warn("ACTIVE tracking_links too low: {}. Consider lowering inactiveLinkRatio={}", activeCnt, inactiveRatio);
        }


        return SeedResult.builder()
                .userId(user.getId())
                .campaigns(campaigns.size())
                .creators(creators.size())
                .channels(channels.size())
                .trackingLinksTotal(totalLinks)
                .trackingLinksActive(activeCnt)
                .trackingLinksInactive(inactiveCnt)
                .inactiveLinkRatioApplied(inactiveRatio)
                .campaignId(campaign.getId()) // 추가 (seed -> seed-clicklogs 사용위해)
                .build();


//        return new SeedResult(
//                user.getId(),
//                campaigns.size(),
//                creators.size(),
//                channels.size(),
//                totalLinks,
//                activeCnt,
//                inactiveCnt,
//                inactiveRatio,
//                campaign.getId()     // 추가 (seed -> seed-clicklogs 사용위해)
//        );
    }

    @Transactional
    public SeedClickLogsResult seedClickLogs(SeedClickLogsRequest req) {
        long start = System.currentTimeMillis();

        // 1) 캠페인 내 ACTIVE tracking_link_id 전부 로드
        List<Long> linkIds = jdbcTemplate.queryForList(
                "SELECT id FROM tracking_links WHERE campaign_id = ? AND status = 'ACTIVE'",
                Long.class,
                req.getCampaignId()
        );

        if (linkIds.isEmpty()) {
            throw new IllegalArgumentException("ACTIVE tracking_links가 없습니다. 먼저 /api/test/seed로 링크를 생성하세요.");
        }

        // 2) “hot links” 풀 구성 (편향)
        List<Long> hot = pickHotLinks(linkIds, req.getHotLinkTopK());
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // 3) 시간 범위 계산 (KST 기준으로 분산)
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(KST);

        // daysBackFrom(예:90) ~ daysBackTo(예:30) 사이 랜덤 날짜/시간
        int from = Math.max(req.getDaysBackFrom(), req.getDaysBackTo());
        int to = Math.min(req.getDaysBackFrom(), req.getDaysBackTo());

        int total = req.getTotalRows();
        int batchSize = Math.max(1000, req.getBatchSize());

        String sql = "INSERT INTO click_logs (clicked_at, ip, referer, user_agent, tracking_link_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        int inserted = 0;

        //offset은 매 반복마다 batchSize만큼 증가한다. 0 -> 5000 -> 10000 -> .. 이런 식으로.
        for (int offset = 0; offset < total; offset += batchSize) {
            //마지막 배치가 남은 수만큼 넣도록. 마지막 찌꺼기 처리하기 위해서. total-offset =2000이면
            //size = min(5000,2000) = 2000 이렇게 딱 totalRows만큼 정확히 넣고 마지막에 남은 게 있어도 처리됨.
            int size = Math.min(batchSize, total - offset);

            // batchUpdate: PreparedStatement 재사용 + 네트워크 왕복 최소화
            //jdbcTemplate.batchUpdate메서드가 모든 걸 처리한다.
            int[] res = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    // ---- tracking_link_id 선택 (skew) ----
                    //1) trackint_link_id 랜덤 선택
                    long trackingLinkId;
                    if (req.getSkewRatio() > 0 && !hot.isEmpty() && rnd.nextDouble() < req.getSkewRatio()) {
                        trackingLinkId = hot.get(rnd.nextInt(hot.size())); //hot에서 꺼냄
                    } else {
                        trackingLinkId = linkIds.get(rnd.nextInt(linkIds.size())); // 전체에서 꺼냄
                    }

                    // ---- clicked_at 분산 ----
                    // 날짜: today - [to..from]일
                    //2) clicked_at 랜덤 날짜/시간
                    int daysBack = rnd.nextInt(to, from + 1);
                    LocalDate d = today.minusDays(daysBack);

                    // 시간: 하루 중 랜덤 (0~86399초)
                    int sec = rnd.nextInt(0, 24 * 60 * 60);
                    LocalDateTime clickedAt = d.atStartOfDay().plusSeconds(sec);

                    // ---- optional columns ----
                    // IP/UA/Referer는 NULL로 넣어도 됨. (desc에서 NULL 허용)
                    ps.setTimestamp(1, Timestamp.valueOf(clickedAt));
                    ps.setString(2, null);   // ip
                    ps.setString(3, null);   // referer
                    ps.setString(4, null);   // user_agent
                    ps.setLong(5, trackingLinkId);//여기까지는 준비만 (DB 전송 아님)
                }

                @Override
                public int getBatchSize() {
                    return size; //5000
                }
            }); //이 줄이 끝나면 이미 DB에 INSERT 완료된다.

//            inserted += Arrays.stream(res).sum();
            inserted += size; //“정확한 inserted”는 필요하면 마지막에 SELECT COUNT(*)로 확인하면 됨.
        }

        long elapsed = System.currentTimeMillis() - start;
        return new SeedClickLogsResult(inserted, elapsed);
    }

    private List<Long> pickHotLinks(List<Long> linkIds, int topK) {
        if (topK <= 0) return Collections.emptyList();
        int k = Math.min(topK, linkIds.size());
        // “상위 K개”의 의미는 사실 랜덤으로 K개 뽑아도 “쏠림” 재현은 됨
        // (진짜 랭킹 기반 hot이 필요하면 click_counts 같은 테이블/쿼리가 있어야 함)
        List<Long> copy = new ArrayList<>(linkIds);
        Collections.shuffle(copy);
        return copy.subList(0, k);
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
            } catch (DataIntegrityViolationException ignored) {
            }
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String generateSlug() {
        // 충돌 확률을 사실상 0으로 만들기 위해 길이를 12로
        //return randomSlug(12);
        return UUID.randomUUID().toString().replace("-", "") + randomSlug(6);
    }

    private String randomSlug(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
