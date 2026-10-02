package com.swyp.FinQ.global.security.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class BackofficeApiKeyAuthenticationFilterTest {

    @Test
    void rejectsBackofficeRequestWhenConfiguredKeyIsTooShort() throws Exception {
        BackofficeApiKeyAuthenticationFilter filter =
                new BackofficeApiKeyAuthenticationFilter("short-key");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/internal/backoffice/statistics"
        );
        request.addHeader(BackofficeApiKeyAuthenticationFilter.HEADER_NAME, "short-key");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(503);
    }

    @Test
    void ignoresRequestsOutsideBackofficePath() throws Exception {
        BackofficeApiKeyAuthenticationFilter filter =
                new BackofficeApiKeyAuthenticationFilter("");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
