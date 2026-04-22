package com.cqf.api.client;

import com.cqf.api.client.fallback.TicketClientFactory;
import com.cqf.api.config.DefaultFeignConfig;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.vo.TicketPendingVo;
import com.cqf.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ticket-service",fallbackFactory = TicketClientFactory.class, configuration = DefaultFeignConfig .class)
public interface TicketClient {
    @GetMapping("/office/ticket/pending")
    Result<PageResult<TicketPendingVo>> pending(@RequestParam("current") Integer current, @RequestParam("size") Integer size);
}
