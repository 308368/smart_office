package com.cqf.api.client;


import com.cqf.api.client.fallback.OfficeClientFactory;
import com.cqf.api.config.DefaultFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "office-service",fallbackFactory = OfficeClientFactory.class, configuration = DefaultFeignConfig.class)
public interface OfficeClient {
    @GetMapping("/office/leave/pendingLeave")
    Long pendingLeave();
}
