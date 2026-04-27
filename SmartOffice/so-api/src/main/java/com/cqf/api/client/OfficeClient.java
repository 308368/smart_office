package com.cqf.api.client;


import com.cqf.api.client.fallback.OfficeClientFactory;
import com.cqf.api.config.DefaultFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "office-service",fallbackFactory = OfficeClientFactory.class, configuration = DefaultFeignConfig.class)
public interface OfficeClient {
    @GetMapping("/office/leave/pendingLeave")
    Long pendingLeave();
    @PostMapping("/office/websocket/sendChunkComplete")
    void sendChunkComplete(
            @RequestParam("kbId") Long kbId,
            @RequestParam("docId") Long docId,
            @RequestParam("docTitle") String docTitle,
            @RequestParam("chunkCount") Integer chunkCount,
            @RequestParam("username") String username
    );
}
