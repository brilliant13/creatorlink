package com.jung.creatorlink.repository.creator;

import com.jung.creatorlink.domain.common.Status;
import com.jung.creatorlink.domain.creator.Creator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreatorRepository extends JpaRepository<Creator, Long> {
    //광고주별 Creator 목록 조회
//    List<Creator> findAllByAdvertiserId(Long advertiserId);
//    List<Creator> findAllByAdvertiserId(Long advertiserId);
    List<Creator> findAllByAdvertiserIdAndStatus(Long advertiserId, Status status);
}
