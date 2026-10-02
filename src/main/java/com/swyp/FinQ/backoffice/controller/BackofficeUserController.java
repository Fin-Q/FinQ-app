package com.swyp.FinQ.backoffice.controller;

import com.swyp.FinQ.backoffice.dto.BackofficeUserListResponse;
import com.swyp.FinQ.backoffice.service.BackofficeUserService;
import com.swyp.FinQ.global.success.GlobalSuccessCode;
import com.swyp.FinQ.global.success.SuccessResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/backoffice/users")
@RequiredArgsConstructor
@Hidden
public class BackofficeUserController {

    private final BackofficeUserService userService;

    @GetMapping
    public ResponseEntity<SuccessResponse<BackofficeUserListResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String query
    ) {
        return SuccessResponse.of(
                GlobalSuccessCode.SUCCESS,
                userService.getUsers(page, size, query)
        );
    }
}
