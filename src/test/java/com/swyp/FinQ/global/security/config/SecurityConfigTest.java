package com.swyp.FinQ.global.security.config;

import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.support.MySqlContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest extends MySqlContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    void returnsCommonErrorResponseWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/knowledge-map"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.traceId").isString());
    }

    @Test
    void authenticatesValidAccessToken() throws Exception {
        IssuedTokenPair tokens = tokenProvider.issue(1L);

        mockMvc.perform(get("/knowledge-map")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk());
    }
}
