package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.config.SesMailProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SesPasswordResetMailSenderTest {

    @Test
    void sendsUtf8VerificationCodeEmail() {
        SesV2Client sesV2Client = mock(SesV2Client.class);
        PasswordResetMailSender mailSender = new SesPasswordResetMailSender(
                sesV2Client,
                new SesMailProperties("ap-northeast-2", "no-reply@finq.example")
        );

        mailSender.sendVerificationCode("user@example.com", "123456", Duration.ofMinutes(5));

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesV2Client).sendEmail(captor.capture());
        SendEmailRequest request = captor.getValue();

        assertThat(request.fromEmailAddress()).isEqualTo("no-reply@finq.example");
        assertThat(request.destination().toAddresses()).containsExactly("user@example.com");
        assertThat(request.content().simple().subject().charset()).isEqualTo("UTF-8");
        assertThat(request.content().simple().body().text().data()).contains("123456", "5분");
    }
}
