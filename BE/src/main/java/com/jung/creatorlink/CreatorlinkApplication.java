package com.jung.creatorlink;

import com.jung.creatorlink.config.props.StatsCacheProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(StatsCacheProperties.class)
@SpringBootApplication
//스프링 부트 메인
public class CreatorlinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreatorlinkApplication.class, args);
    }

}
