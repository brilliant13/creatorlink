package com.jung.creatorlink.controller.tracking;


import com.jung.creatorlink.service.tracking.TrackingLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
// /t/{slug} 리다이렉트 엔드포인트
@Tag(name = "RedirectController", description = "/t/{slug} 리다이렉트 엔드포인트")
public class RedirectController {

    private final TrackingLinkService trackingLinkService;

    //실제 유저가 클릭하는 엔드포인트: /t/{slug}
    @GetMapping("/t/{slug}")
//    @Operation(summary = "실제 유저가 클릭하는 엔드포인트", description = "/t/{slug}")
    @Operation(summary = "트래킹 링크 리다이렉트", description = "slug로 최종 URL로 302 리다이렉트한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "최종 URL로 리다이렉트"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 slug"),
    })
    public ResponseEntity<Void> redirect(@PathVariable String slug, HttpServletRequest request) {
        String finalUrl = trackingLinkService.handleClick(slug, request);

        return ResponseEntity.status(HttpStatus.FOUND) //302
                .location(URI.create(finalUrl))
                .build();
    }
}

