package com.swyp.FinQ.user.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.user.config.SesMailProperties;
import com.swyp.FinQ.user.exception.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SesPasswordResetMailSender implements PasswordResetMailSender {

    private static final String SUBJECT = "[FinQ] 비밀번호 재설정 인증번호";

    private final SesV2Client sesV2Client;
    private final SesMailProperties properties;

    @Override
    public void sendVerificationCode(String recipient, String verificationCode, Duration expiration) {
        String body = "FinQ 비밀번호 재설정 인증번호는 %s입니다.\n인증번호는 %d분 동안 유효합니다."
                .formatted(verificationCode, expiration.toMinutes());

        SendEmailRequest request = SendEmailRequest.builder()
                .fromEmailAddress(properties.fromAddress())
                .destination(destination -> destination.toAddresses(recipient))
                .content(content -> content.simple(message -> message
                        .subject(subject -> subject
                                .charset(StandardCharsets.UTF_8.name())
                                .data(SUBJECT))
                        .body(messageBody -> messageBody.text(text -> text
                                .charset(StandardCharsets.UTF_8.name())
                                .data(body)))))
                .build();

        try {
            sesV2Client.sendEmail(request);
        } catch (SesV2Exception | SdkClientException exception) {
            throw new BaseException(AuthErrorCode.PASSWORD_RESET_EMAIL_SEND_FAILED, exception);
        }
    }
}
