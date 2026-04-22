package com.cqf.api.client;

import com.cqf.api.client.fallback.AuthClientFallbackFactory;
import com.cqf.api.client.fallback.KbDocumentClientFactory;
import com.cqf.api.config.DefaultFeignConfig;
import com.cqf.common.domain.dto.ChunkSaveRequest;
import com.cqf.common.domain.vo.DocumentVo;
import com.cqf.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "knowledge-service",fallbackFactory = KbDocumentClientFactory.class, configuration = DefaultFeignConfig.class)
public interface KbDocumentClient {
    @GetMapping("/knowledge/kb/userDoc/{isOwe}")
    List<DocumentVo> userDoc(@PathVariable("isOwe") Integer isOwe);
    @PostMapping("/knowledge/kb/chunk/batch")
    Result saveBatch(@RequestBody ChunkSaveRequest request);
    @GetMapping("/knowledge/kb/total")
    Long total();
}
