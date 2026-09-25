package com.swyp.FinQ.global.security.config;

import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.support.MySqlContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void allowsGuestKnowledgeMapWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/knowledge-map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.categories").isArray());
    }

    @Test
    void authenticatesValidAccessToken() throws Exception {
        IssuedTokenPair tokens = tokenProvider.issue(1L);

        mockMvc.perform(get("/knowledge-map")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void allowsGuestContentAnswerWithoutAccessToken() throws Exception {
        mockMvc.perform(post("/contents/1/questions/1/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedOptionId\":\"X\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.correct").value(true))
                .andExpect(jsonPath("$.data.nextAction").value("NEXT_BODY"))
                .andExpect(jsonPath("$.data.contentResult").isEmpty());
    }
}
