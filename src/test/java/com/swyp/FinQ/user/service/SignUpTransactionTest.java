package com.swyp.FinQ.user.service;

import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.dto.req.AgreementRequest;
import com.swyp.FinQ.user.dto.req.SignUpRequest;
import com.swyp.FinQ.user.repository.UserAgreementRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class SignUpTransactionTest extends MySqlContainerSupport {

    @Autowired
    private SignUpService signUpService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UserAgreementRepository userAgreementRepository;

    @Test
    void rollsBackUserWhenAgreementPersistenceFails() {
        given(userAgreementRepository.saveAll(anyList()))
                .willThrow(new IllegalStateException("약관 저장 실패"));
        SignUpRequest request = new SignUpRequest(
                "rollback@example.com",
                "Password123!",
                "Minter",
                List.of(
                        new AgreementRequest("TERMS_OF_SERVICE", "1.0", true),
                        new AgreementRequest("PRIVACY_POLICY", "1.0", true)
                )
        );

        assertThatThrownBy(() -> signUpService.signUp(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("약관 저장 실패");

        assertThat(userRepository.findByEmail("rollback@example.com")).isEmpty();
    }
}
