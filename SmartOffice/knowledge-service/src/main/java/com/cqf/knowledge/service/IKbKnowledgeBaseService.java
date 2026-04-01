package com.cqf.knowledge.service;

import com.cqf.common.domain.PageResult;
import com.cqf.knowledge.model.dto.KnowledgeQueryParam;
import com.cqf.knowledge.model.po.KbKnowledgeBase;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.knowledge.model.vo.KnowledgeVO;

/**
 * <p>
 * 知识库表 服务类
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
public interface IKbKnowledgeBaseService extends IService<KbKnowledgeBase> {

    PageResult<KnowledgeVO> listKnowledge(KnowledgeQueryParam knowledgeQueryParam);

    PageResult<KnowledgeVO> userListKnowledge(Long userId,KnowledgeQueryParam  knowledgeQueryParam);
}
