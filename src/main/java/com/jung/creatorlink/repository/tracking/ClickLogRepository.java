package com.jung.creatorlink.repository.tracking;

import com.jung.creatorlink.domain.tracking.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {

}
