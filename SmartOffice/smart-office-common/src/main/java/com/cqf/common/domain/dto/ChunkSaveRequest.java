package com.cqf.common.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChunkSaveRequest {

    private List<ChunkItem> chunks;

    private String username;

    @Data
    public static class ChunkItem {
        private Long documentId;
        private String chunkContent;
        private Integer chunkIndex;
        private String vectorId;
    }
}
