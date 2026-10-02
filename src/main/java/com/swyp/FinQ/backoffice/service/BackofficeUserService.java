package com.swyp.FinQ.backoffice.service;

import com.swyp.FinQ.backoffice.dto.BackofficeUserListResponse;
import com.swyp.FinQ.backoffice.repository.BackofficeUserQueryRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackofficeUserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BackofficeUserQueryRepository repository;

    @Transactional(readOnly = true)
    public BackofficeUserListResponse getUsers(int page, int size, String rawQuery) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw BaseException.of(
                    GlobalErrorCode.COMMON_INVALID_REQUEST,
                    "페이지는 0 이상, 페이지 크기는 1 이상 100 이하여야 합니다."
            );
        }

        String query = rawQuery == null ? "" : rawQuery.trim();
        long totalElements = repository.countUsers(query);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        return new BackofficeUserListResponse(
                repository.findUsers(query, page * size, size),
                totalElements,
                page,
                size,
                totalPages
        );
    }
}
