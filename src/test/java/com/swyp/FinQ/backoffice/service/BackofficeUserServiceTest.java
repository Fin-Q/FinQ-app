package com.swyp.FinQ.backoffice.service;

import com.swyp.FinQ.backoffice.dto.BackofficeUserListResponse;
import com.swyp.FinQ.backoffice.repository.BackofficeUserQueryRepository;
import com.swyp.FinQ.global.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackofficeUserServiceTest {

    @Mock
    private BackofficeUserQueryRepository repository;

    @InjectMocks
    private BackofficeUserService service;

    @Test
    void returnsPagedUsersWithTrimmedQuery() {
        when(repository.countUsers("민지")).thenReturn(21L);
        when(repository.findUsers("민지", 20, 20)).thenReturn(List.of());

        BackofficeUserListResponse response = service.getUsers(1, 20, "  민지  ");

        assertThat(response.totalElements()).isEqualTo(21L);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.page()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidPageSize() {
        assertThatThrownBy(() -> service.getUsers(0, 101, ""))
                .isInstanceOf(BaseException.class);
    }
}
