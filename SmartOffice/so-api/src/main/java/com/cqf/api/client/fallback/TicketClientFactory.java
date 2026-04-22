package com.cqf.api.client.fallback;

import com.cqf.api.client.TicketClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.vo.TicketPendingVo;
import com.cqf.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TicketClientFactory implements FallbackFactory<TicketClient> {
    @Override
    public TicketClient create(Throwable cause) {
        return new TicketClient() {
            @Override
            public Result<PageResult<TicketPendingVo>> pending(Integer current, Integer size) {
                log.info("工单服务异常{}",cause.getMessage());
                return null;
            }
        };
    }
}
