package com.jung.creatorlink.loadTest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TestTokenGuard {
    @Value("${app.test.token}")
    private String expected;

    //    public void check(String actual) {
//        if (actual == null || !actual.equals(expected)) {
//            throw new IllegalArgumentException("Invalid test token");
//        }
//    }
    public void check(String actual) {
        if (actual == null || !actual.equals(expected)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,  // 401
                    "Unauthorized"
            );
        }
    }
}
