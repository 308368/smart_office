package com.cqf.ai.model.vo;

import lombok.Data;

@Data
public class KbSimpleVO {
    private Long docId;
    private String docTitle;
    private Long kbId;
    private String kbName;
    private String fileType;
}
