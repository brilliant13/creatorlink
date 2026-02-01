package com.jung.creatorlink.config.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.stats-cache")
public class StatsCacheProperties {
    private boolean enabled = false;
    private long ttlSeconds = 60;
}
