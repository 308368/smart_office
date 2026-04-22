package com.cqf.api.client;

import com.cqf.api.client.fallback.VectorFeignClientFactory;
import com.cqf.api.config.DefaultFeignConfig;
import com.cqf.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "ai-service",fallbackFactory = VectorFeignClientFactory.class, configuration = DefaultFeignConfig.class)
public interface VectorFeignClient {
    @DeleteMapping("/ai/vector/delete")
    Result deleteVectors(@RequestParam List<String> vectorIds);
    @GetMapping("/ai/chat/count")
    Long count();
}
