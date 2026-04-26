package com.cqf.ai.model.dto;

import lombok.Data;

@Data
public class AddPromptDTO {
    private String name;
    private String description;
    private String prompt;
    private String category;
    private Integer isPublic;
}
