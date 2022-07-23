package com.nhnacademy.marketgg.auth.service.impl;

import com.nhnacademy.marketgg.auth.dto.response.MemberResponse;
import com.nhnacademy.marketgg.auth.entity.Auth;
import com.nhnacademy.marketgg.auth.exception.AuthNotFoundException;
import com.nhnacademy.marketgg.auth.exception.UnAuthorizationException;
import com.nhnacademy.marketgg.auth.jwt.TokenUtils;
import com.nhnacademy.marketgg.auth.repository.AuthRepository;
import com.nhnacademy.marketgg.auth.service.AuthInfoService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultAuthInfoService implements AuthInfoService {

    private final TokenUtils tokenUtils;
    private final AuthRepository authRepository;

    @Override
    public MemberResponse findAuthByUuid(final String token) throws UnAuthorizationException {
        if (Objects.isNull(token) || tokenUtils.isInvalidToken(token)) {
            throw new UnAuthorizationException();
        }

        String jwt = token.substring(TokenUtils.BEARER_LENGTH);
        String uuid = tokenUtils.getUuidFromToken(jwt);

        Auth auth = authRepository.findByUuid(uuid)
                                  .orElseThrow(AuthNotFoundException::new);

        return new MemberResponse(auth.getEmail(), auth.getName(), auth.getPhoneNumber());

    }

}
