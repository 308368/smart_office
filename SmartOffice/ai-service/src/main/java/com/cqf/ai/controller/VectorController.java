package com.cqf.ai.controller;

import com.cqf.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai/vector")
@RequiredArgsConstructor
public class VectorController {
    private final ElasticsearchVectorStore elasticsearchVectorStore;

    @DeleteMapping("/delete")
    public Result deleteVectors(@RequestParam List<String> vectorIds) {
        log.info("删除向量, vectorIds: {}", vectorIds);
        try {
            elasticsearchVectorStore.delete(vectorIds);
            return Result.success();
        } catch (Exception e) {
            log.error("删除向量失败", e);
            return Result.error("删除向量失败");
        }
    }
}