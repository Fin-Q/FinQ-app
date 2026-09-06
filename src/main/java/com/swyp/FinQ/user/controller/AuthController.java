package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.user.dto.req.SignUpRequest;
import com.swyp.FinQ.user.dto.res.SignUpResponse;
import com.swyp.FinQ.user.service.SignUpService;
import com.swyp.FinQ.user.success.AuthSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원 인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignUpService signUpService;

    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        return SuccessResponse.of(AuthSuccessCode.SIGN_UP, signUpService.signUp(request));
    }
}
