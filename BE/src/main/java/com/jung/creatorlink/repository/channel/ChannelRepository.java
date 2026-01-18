package com.jung.creatorlink.repository.channel;

import com.jung.creatorlink.domain.channel.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jung.creatorlink.domain.common.Status;


import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    boolean existsByAdvertiser_IdAndPlatformAndPlacement(Long advertiserId, String platform, String placement);

    List<Channel> findAllByAdvertiser_IdAndStatus(Long advertiserId, Status status);

    boolean existsByAdvertiser_IdAndPlatformAndPlacementAndIdNot(
            Long advertiserId, String platform, String placement, Long id
    );
    boolean existsByAdvertiser_IdAndPlatformAndPlacementAndStatusAndIdNot(
            Long advertiserId, String platform, String placement, Status status, Long id
    );
    Optional<Channel> findByAdvertiser_IdAndPlatformAndPlacement(Long advertiserId, String platform, String placement);

}