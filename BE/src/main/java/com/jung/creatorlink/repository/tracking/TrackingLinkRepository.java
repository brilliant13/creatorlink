package com.jung.creatorlink.repository.tracking;

import com.jung.creatorlink.domain.tracking.TrackingLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackingLinkRepository extends JpaRepository<TrackingLink, Long> {

    Optional<TrackingLink> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
