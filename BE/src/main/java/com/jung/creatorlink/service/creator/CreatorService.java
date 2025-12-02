package com.jung.creatorlink.service.creator;

import com.jung.creatorlink.domain.creator.Creator;
import com.jung.creatorlink.domain.user.User;
import com.jung.creatorlink.dto.creator.CreatorCreateRequest;
import com.jung.creatorlink.dto.creator.CreatorResponse;
import com.jung.creatorlink.repository.creator.CreatorRepository;
import com.jung.creatorlink.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatorService {

    private final CreatorRepository creatorRepository;
    private final UserRepository userRepository;

    //크리에이터 생성
    public CreatorResponse createCreator(CreatorCreateRequest request) {
        //1. 광고주(User) 조회
        User advertiser = userRepository.findById(request.getAdvertiserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고아고주입니다."));

        //2. 엔티티 생성
        Creator creator = Creator.builder()
                .advertiser(advertiser)
                .name(request.getName())
                .channelName(request.getChannelName())
                .channelUrl(request.getChannelUrl())
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();

        //3. 저장
        Creator saved = creatorRepository.save(creator);

        //4. 응답 DTO로 변환
        return toResponse(saved);
    }

    //광고주별 Creator 목록 조회
    @Transactional(readOnly = true)
    public List<CreatorResponse> getCreatorsByAdvertiser(Long advertiserId) {
        List<Creator> creators = creatorRepository.findAllByAdvertiserId(advertiserId);
        return creators.stream()
                .map(this::toResponse)
                .toList();
    }

    //단건 조회
    @Transactional(readOnly = true)
    public CreatorResponse getCreator(Long id, Long advertiserId) {
        Creator creator = creatorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("크리에이터를 찾을 수 없습니다."));


        if (!creator.getAdvertiser().getId().equals(advertiserId)) {
            throw new IllegalArgumentException("해당 크리에이터에 접근할 권한이 없습니다.");
        }
        return toResponse(creator);
    }

    private CreatorResponse toResponse(Creator creator) {
        return CreatorResponse.builder()
                .id(creator.getId())
                .advertiserId(creator.getAdvertiser().getId())
                .name(creator.getName())
                .channelName(creator.getChannelName())
                .channelUrl(creator.getChannelUrl())
                .note(creator.getNote())
                .build();
    }
    //여기서도 advertiserId는 지금은 인증이 없어서 쿼리/요청으로 받는 임시 방식이야.
    //나중에 JWT 붙이면 “현재 로그인된 User ID”를 뽑아서 쓰는 걸로 바꿀 거고.
}
