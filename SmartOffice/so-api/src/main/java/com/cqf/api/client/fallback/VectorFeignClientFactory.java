package com.cqf.api.client.fallback;

import com.cqf.api.client.VectorFeignClient;
import com.cqf.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class VectorFeignClientFactory implements FallbackFactory<VectorFeignClient> {
    @Override
    public VectorFeignClient create(Throwable cause) {
        return new VectorFeignClient() {
            @Override
            public Result deleteVectors(List<String> vectorIds) {
                log.info("删除向量异常{}",cause.getMessage());
                log.error("deleteVectors error:{}", cause.getMessage());
                return null;
            }
        };
    }
}
