package com.swyp.FinQ.backoffice.repository;

import com.swyp.FinQ.backoffice.dto.BackofficeUserListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BackofficeUserQueryRepository {

    private final JdbcClient jdbcClient;

    public long countUsers(String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        return jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM users
                        WHERE :query = ''
                           OR LOWER(nickname) LIKE :pattern
                           OR LOWER(email) LIKE :pattern
                        """)
                .param("query", query)
                .param("pattern", pattern)
                .query(Long.class)
                .single();
    }

    public List<BackofficeUserListResponse.UserItem> findUsers(
            String query,
            int offset,
            int size
    ) {
        String pattern = "%" + query.toLowerCase() + "%";
        return jdbcClient.sql("""
                        SELECT user_id,
                               nickname,
                               email,
                               onboarding_status,
                               total_xp,
                               current_streak,
                               created_at,
                               last_login_at
                        FROM users
                        WHERE :query = ''
                           OR LOWER(nickname) LIKE :pattern
                           OR LOWER(email) LIKE :pattern
                        ORDER BY created_at DESC, user_id DESC
                        LIMIT :size OFFSET :offset
                        """)
                .param("query", query)
                .param("pattern", pattern)
                .param("size", size)
                .param("offset", offset)
                .query((resultSet, rowNumber) -> new BackofficeUserListResponse.UserItem(
                        resultSet.getLong("user_id"),
                        resultSet.getString("nickname"),
                        resultSet.getString("email"),
                        resultSet.getString("onboarding_status"),
                        resultSet.getInt("total_xp"),
                        resultSet.getInt("current_streak"),
                        resultSet.getTimestamp("created_at").toLocalDateTime(),
                        resultSet.getTimestamp("last_login_at") == null
                                ? null
                                : resultSet.getTimestamp("last_login_at").toLocalDateTime()
                ))
                .list();
    }
}
