package com.nhnacademy.marketgg.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nhnacademy.marketgg.auth.config.WebSecurityConfig;
import com.nhnacademy.marketgg.auth.dto.response.TokenResponse;
import com.nhnacademy.marketgg.auth.jwt.TokenUtils;
import com.nhnacademy.marketgg.auth.repository.AuthRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@Import({
    WebSecurityConfig.class
})
class DefaultAuthServiceTest {

    @InjectMocks
    DefaultAuthService authService;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    TokenUtils tokenUtils;

    @Test
    @DisplayName("๋ก๊ทธ์์")
    void testLogout() {
        String jwt = "jwt";
        String uuid = UUID.randomUUID().toString();

        given(tokenUtils.getUuidFromToken(jwt)).willReturn(uuid);
        lenient().when(tokenUtils.isInvalidToken(jwt)).thenReturn(false);

        HashOperations<String, Object, Object> mockHash
            = mock(HashOperations.class);

        given(redisTemplate.opsForHash()).willReturn(mockHash);
        given(mockHash.delete(uuid, TokenUtils.REFRESH_TOKEN)).willReturn(0L);

        given(tokenUtils.getExpireDate(jwt)).willReturn(System.currentTimeMillis() + 1000L);

        ValueOperations<String, Object> mockValue = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(mockValue);
        doNothing().when(mockValue).set(anyString(), anyBoolean(), anyLong(), any(TimeUnit.class));

        authService.logout(jwt);

        verify(mockHash).delete(uuid, TokenUtils.REFRESH_TOKEN);
        verify(mockValue).set(anyString(), anyBoolean(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("๋ง๋ฃ๋ ํ?ํฐ์ ๊ฐ์ง ์ฌ์ฉ์๊ฐ ๋ก๊ทธ์์")
    void testLogoutWithInvalidJWT() {
        String jwt = "jwt";
        String uuid = UUID.randomUUID().toString();

        given(tokenUtils.getUuidFromToken(jwt)).willReturn(uuid);
        lenient().when(tokenUtils.isInvalidToken(jwt)).thenReturn(true);

        HashOperations<String, Object, Object> mockHash
            = mock(HashOperations.class);

        given(redisTemplate.opsForHash()).willReturn(mockHash);
        given(mockHash.delete(uuid, TokenUtils.REFRESH_TOKEN)).willReturn(0L);

        ValueOperations<String, Object> mockValue = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(mockValue);
        willDoNothing().given(mockValue).set(any(), anyBoolean(), anyLong(), any(TimeUnit.class));

        authService.logout(jwt);

        verify(mockHash).delete(uuid, TokenUtils.REFRESH_TOKEN);
    }

    @Test
    @DisplayName("ํ?ํฐ ์ฌ๋ฐ๊ธ")
    void testRenewToken() {
        String uuid = UUID.randomUUID().toString();
        String jwt = "jwt";
        String refreshToken = "refreshToken";

        given(tokenUtils.getUuidFromToken(jwt)).willReturn(uuid);

        HashOperations<String, Object, Object> mockHash
            = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(mockHash);
        given(mockHash.get(uuid, TokenUtils.REFRESH_TOKEN)).willReturn(refreshToken);

        given(tokenUtils.isInvalidToken(refreshToken)).willReturn(false);
        given(tokenUtils.getUuidFromToken(refreshToken)).willReturn(uuid);

        when(mockHash.delete(uuid, TokenUtils.REFRESH_TOKEN)).thenReturn(0L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(uuid, "");

        LocalDateTime now = LocalDateTime.now();
        given(tokenUtils.getAuthenticationFromExpiredToken(jwt, uuid)).willReturn(authentication);
        given(tokenUtils.saveRefreshToken(redisTemplate, authentication)).willReturn(
            new TokenResponse(jwt, now));

        TokenResponse tokenResponse = authService.renewToken(jwt);

        verify(mockHash, times(1)).get(uuid, TokenUtils.REFRESH_TOKEN);
        verify(mockHash, times(1)).delete(uuid, TokenUtils.REFRESH_TOKEN);

        assertThat(tokenResponse.getJwt()).isEqualTo(jwt);
        assertThat(tokenResponse.getExpiredDate().toString()).hasToString(now.toString());
    }

    @Test
    @DisplayName("ํ?ํฐ ์ฌ๋ฐ๊ธ ์ ๋ฆฌํ๋?์ ํ?ํฐ ๋ง๋ฃ๋ก ์ฌ๋ฐ๊ธ ๋ชป๋ฐ์")
    void testRenewTokenFail() {
        String uuid = UUID.randomUUID().toString();
        String jwt = "jwt";
        String refreshToken = "refreshToken";

        given(tokenUtils.getUuidFromToken(jwt)).willReturn(uuid);

        HashOperations<String, Object, Object> mockHash
            = mock(HashOperations.class);
        given(redisTemplate.opsForHash()).willReturn(mockHash);
        given(mockHash.get(uuid, TokenUtils.REFRESH_TOKEN)).willReturn(refreshToken);

        given(tokenUtils.isInvalidToken(refreshToken)).willReturn(true);

        TokenResponse tokenResponse = authService.renewToken(jwt);

        assertThat(tokenResponse).isNull();
    }

}