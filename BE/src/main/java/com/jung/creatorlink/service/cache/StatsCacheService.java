package com.jung.creatorlink.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jung.creatorlink.config.props.StatsCacheProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsCacheService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final StatsCacheProperties props;

// 단일 객체용
public <T> Optional<T> get(String key, Class<T> type) {
    if (!props.isEnabled()) return Optional.empty();

    String json = redisTemplate.opsForValue().get(key);
    if (json == null) {
        log.debug("Cache MISS: {}", key);
        return Optional.empty();
    }

    try {
        log.debug("Cache HIT: {}", key);
        return Optional.of(objectMapper.readValue(json, type));
    } catch (Exception e) {
        log.warn("Cache deserialization failed for key: {}", key, e);
        return Optional.empty();
    }
}

    // List 등 제네릭 타입용 //Java의 Type Erasure 때문. Class<T>로는 List 안에 뭐가 들어있는지를 Jackson에게 알려줄 방법이 없다.
    //따라서 Jackson이 제공하는 TypeReference를 사용한다.
    public <T> Optional<T> get(String key, TypeReference<T> typeRef) {
        if (!props.isEnabled()) return Optional.empty();

        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            log.debug("Cache MISS: {}", key);
            return Optional.empty();
        }

        try {
            log.debug("Cache HIT: {}", key);
            return Optional.of(objectMapper.readValue(json, typeRef));
        } catch (Exception e) {
            log.warn("Cache deserialization failed for key: {}", key, e);
            return Optional.empty(); //예외 대신 빈 값 반환 -> DB fallback
        }
    }

    public void set(String key, Object value) {
        if (!props.isEnabled()) return;

        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(
                    key,
                    json,
                    Duration.ofSeconds(props.getTtlSeconds())
            );
            log.debug("Cache SET: {} (TTL: {}s)", key, props.getTtlSeconds());
        } catch (Exception e) {
            log.warn("Cache serialization failed for key: {}", key, e); // 캐시 실패는 기능 실패로 치지 않음
        }
    }

}
