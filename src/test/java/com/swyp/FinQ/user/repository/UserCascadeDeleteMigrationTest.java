package com.swyp.FinQ.user.repository;

import com.swyp.FinQ.support.MySqlContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserCascadeDeleteMigrationTest extends MySqlContainerSupport {

    private static final Map<String, String> EXPECTED_DELETE_RULES = Map.of(
            "fk_user_agreement_user", "CASCADE",
            "fk_social_account_user", "CASCADE",
            "fk_refresh_token_user", "CASCADE",
            "fk_password_reset_request_user", "CASCADE",
            "fk_user_interest_user", "CASCADE",
            "fk_user_content_completion_user", "CASCADE",
            "fk_user_category_completion_user", "CASCADE",
            "fk_xp_history_user", "CASCADE"
    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesCascadeDeleteToEveryUserForeignKey() {
        Map<String, String> deleteRules = jdbcTemplate.queryForList("""
                        SELECT CONSTRAINT_NAME, DELETE_RULE
                        FROM information_schema.REFERENTIAL_CONSTRAINTS
                        WHERE CONSTRAINT_SCHEMA = DATABASE()
                          AND REFERENCED_TABLE_NAME = 'users'
                        """).stream()
                .collect(Collectors.toMap(
                        row -> (String) row.get("CONSTRAINT_NAME"),
                        row -> (String) row.get("DELETE_RULE")
                ));

        assertThat(deleteRules).containsExactlyInAnyOrderEntriesOf(EXPECTED_DELETE_RULES);
    }
}
