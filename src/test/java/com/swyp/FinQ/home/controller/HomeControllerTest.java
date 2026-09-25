package com.swyp.FinQ.home.controller;

import com.swyp.FinQ.content.domain.Category;
import com.swyp.FinQ.content.domain.CategoryCode;
import com.swyp.FinQ.content.repository.CategoryRepository;
import com.swyp.FinQ.global.security.token.IssuedTokenPair;
import com.swyp.FinQ.global.security.token.JwtTokenProvider;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.UserInterest;
import com.swyp.FinQ.user.repository.UserInterestRepository;
import com.swyp.FinQ.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HomeControllerTest extends MySqlContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInterestRepository userInterestRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void returnsThreeRecommendedQuestionsInResponsePayload() throws Exception {
        User user = userRepository.saveAndFlush(User.builder()
                .email("home-controller@example.com")
                .password("encoded-password")
                .nickname("Minter")
                .profileImageCode(ProfileImageCode.PROFILE_01)
                .build());
        Category category = categoryRepository.findByCategoryCode(CategoryCode.SAL).orElseThrow();
        userInterestRepository.saveAndFlush(UserInterest.builder()
                .user(user)
                .category(category)
                .build());

        mockMvc.perform(get("/home")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("홈 화면 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.userMode").value("MEMBER"))
                .andExpect(jsonPath("$.data.questions").isArray())
                .andExpect(jsonPath("$.data.questions.length()").value(3))
                .andExpect(jsonPath("$.data.questions[0].contentId").isNumber())
                .andExpect(jsonPath("$.data.questions[0].categoryCode").value("SAL"))
                .andExpect(jsonPath("$.data.questions[0].title").isString())
                .andExpect(jsonPath("$.data.questions[0].completionStatus").value("INCOMPLETE"));
    }

    @Test
    void returnsGuestHomeWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.userMode").value("GUEST"))
                .andExpect(jsonPath("$.data.nickname")
                        .value("로그인하고 나만의 캐릭터를 키워보세요"))
                .andExpect(jsonPath("$.data.level").value(1))
                .andExpect(jsonPath("$.data.characterStage").value(1))
                .andExpect(jsonPath("$.data.characterImageUrl").isString())
                .andExpect(jsonPath("$.data.totalXp").value(0))
                .andExpect(jsonPath("$.data.currentStreak").value(0))
                .andExpect(jsonPath("$.data.questions").isEmpty());
    }

    @Test
    void rejectsInvalidAccessTokenInsteadOfFallingBackToGuest() throws Exception {
        mockMvc.perform(get("/home")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"));
    }

    private String bearerToken(Long userId) {
        IssuedTokenPair tokens = jwtTokenProvider.issue(userId);
        return "Bearer " + tokens.accessToken();
    }
}
