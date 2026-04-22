package com.cqf.api.client.fallback;

import com.cqf.api.client.OfficeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OfficeClientFactory implements FallbackFactory<OfficeClient> {
    @Override
    public OfficeClient create(Throwable cause) {
        return new OfficeClient() {
            @Override
            public Long pendingLeave() {
                log.info("办公服务异常{}",cause.getMessage());
                log.error("pendingLeave error:{}", cause.getMessage());
                return 0L;
            }
        };
    }
}
