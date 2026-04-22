package com.cqf.api.client.fallback;

import com.cqf.api.client.KbDocumentClient;
import com.cqf.common.domain.dto.ChunkSaveRequest;
import com.cqf.common.domain.vo.DocumentVo;
import com.cqf.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KbDocumentClientFactory implements FallbackFactory<KbDocumentClient> {
    @Override
    public KbDocumentClient create(Throwable cause) {
        return new KbDocumentClient() {
            @Override
            public List<DocumentVo> userDoc(Integer isOwe) {
                log.info("知识服务异常{}",cause.getMessage());
                log.error("userDoc error:{}", cause.getMessage());
                return null;
            }

            @Override
            public Result saveBatch(ChunkSaveRequest request) {
                log.info("知识库服务异常{}",cause.getMessage());
                log.error("saveBatch error:{}", cause.getMessage());
                return Result.error("知识库服务异常");
            }

            @Override
            public Long total() {
                log.info("知识库服务异常{}",cause.getMessage());
                log.error("total error:{}", cause.getMessage());
                return 0L;
            }
        };
    }
}
