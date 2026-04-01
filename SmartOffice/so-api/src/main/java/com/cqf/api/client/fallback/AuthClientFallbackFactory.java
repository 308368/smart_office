package com.cqf.api.client.fallback;

import com.cqf.api.client.AuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthClientFallbackFactory implements FallbackFactory<AuthClient> {
    @Override
    public AuthClient create(Throwable cause) {
        return new AuthClient() {
            @Override
            public Long getUserId(String username) {
                log.info("认证服务异常{}",cause.getMessage());
                log.error("getUserId error:{}", cause.getMessage());
                return -1L;
            }
        };
    }
}
