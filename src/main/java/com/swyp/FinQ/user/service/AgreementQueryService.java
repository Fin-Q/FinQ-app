package com.swyp.FinQ.user.service;

import com.swyp.FinQ.user.domain.AgreementPolicy;
import com.swyp.FinQ.user.dto.res.AgreementListResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional(readOnly = true)
public class AgreementQueryService {

    public AgreementListResponse getCurrentAgreements() {
        return AgreementListResponse.from(Arrays.asList(AgreementPolicy.values()));
    }
}
