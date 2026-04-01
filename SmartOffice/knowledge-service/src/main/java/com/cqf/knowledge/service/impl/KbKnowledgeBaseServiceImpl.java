package com.cqf.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.common.domain.PageResult;
import com.cqf.common.enums.KnowledgeStatusEnum;
import com.cqf.knowledge.model.dto.KnowledgeQueryParam;
import com.cqf.knowledge.model.po.KbKnowledgeBase;
import com.cqf.knowledge.mapper.KbKnowledgeBaseMapper;
import com.cqf.knowledge.model.vo.KnowledgeVO;
import com.cqf.knowledge.service.IKbKnowledgeBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 知识库表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-31
 */
@Service
@RequiredArgsConstructor
public class KbKnowledgeBaseServiceImpl extends ServiceImpl<KbKnowledgeBaseMapper, KbKnowledgeBase> implements IKbKnowledgeBaseService {
    private final KbKnowledgeBaseMapper kbKnowledgeBaseMapper;

    @Override
    public PageResult<KnowledgeVO> listKnowledge(KnowledgeQueryParam knowledgeQueryParam) {
        Page<KbKnowledgeBase> kbKnowledgeBasePage = new Page<>();
        kbKnowledgeBasePage.setCurrent(knowledgeQueryParam.getCurrent());
        kbKnowledgeBasePage.setSize(knowledgeQueryParam.getSize());
        Page<KbKnowledgeBase> page = kbKnowledgeBaseMapper.selectPage(kbKnowledgeBasePage, new LambdaQueryWrapper<KbKnowledgeBase>()
                .eq(StringUtils.isNotBlank(knowledgeQueryParam.getName()), KbKnowledgeBase::getName, knowledgeQueryParam.getName()));
        return getPageResult( page);
    }

    @Override
    public PageResult<KnowledgeVO> userListKnowledge(Long userId,KnowledgeQueryParam  knowledgeQueryParam) {
        if (userId == null)return null;
        LambdaQueryWrapper<KbKnowledgeBase> eq = new LambdaQueryWrapper<KbKnowledgeBase>()
                .eq(KbKnowledgeBase::getCreateBy, userId)
                .or()
                .eq(KbKnowledgeBase::getStatus, KnowledgeStatusEnum.PUBLIC.getStatus());
        Page<KbKnowledgeBase> kbKnowledgeBasePage = kbKnowledgeBaseMapper.selectPage(new Page<>(knowledgeQueryParam.getCurrent(), knowledgeQueryParam.getSize()), eq);
        return getPageResult(kbKnowledgeBasePage);
    }

    @NotNull
    private static PageResult<KnowledgeVO> getPageResult(Page<KbKnowledgeBase> kbKnowledgeBasePage) {
        PageResult<KnowledgeVO> pageResult = new PageResult<>();
        List<KbKnowledgeBase> records = kbKnowledgeBasePage.getRecords();
        List<KnowledgeVO> knowledgeVOS = BeanUtil.copyToList(records, KnowledgeVO.class);
        pageResult.setRecords(knowledgeVOS);
        pageResult.setPages((int) kbKnowledgeBasePage.getPages());
        pageResult.setCurrent((int) kbKnowledgeBasePage.getCurrent());
        pageResult.setSize((int) kbKnowledgeBasePage.getSize());
        pageResult.setTotal((int) kbKnowledgeBasePage.getTotal());
        return pageResult;
    }
}
