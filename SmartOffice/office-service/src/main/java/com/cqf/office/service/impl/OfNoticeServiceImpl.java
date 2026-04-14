package com.cqf.office.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.common.domain.po.SysUser;
import com.cqf.office.enums.NoticeStatusEnum;
import com.cqf.office.mapper.OfNoticeReadMapper;
import com.cqf.office.model.dto.AUNoticeDTO;
import com.cqf.office.model.dto.NoticeDTO;
import com.cqf.office.model.po.OfNotice;
import com.cqf.office.mapper.OfNoticeMapper;
import com.cqf.office.model.po.OfNoticeRead;
import com.cqf.office.service.IOfNoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 通知公告表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-14
 */
@Service
@RequiredArgsConstructor
public class OfNoticeServiceImpl extends ServiceImpl<OfNoticeMapper, OfNotice> implements IOfNoticeService {
    private final OfNoticeMapper noticeMapper;
    private final OfNoticeReadMapper noticeReadMapper;

    @Override
    @Transactional
    public NoticeDTO readNotice(Long id,Long userId) {
        OfNotice ofNotice = noticeMapper.selectById(id);
        if (ofNotice.getPublishStatus()== NoticeStatusEnum.DRAFT.getStatus())return BeanUtil.copyProperties(ofNotice, NoticeDTO.class);
        //阅读数加1
        ofNotice.setViewCount(ofNotice.getViewCount() + 1);
        int i = noticeMapper.updateById(ofNotice);
        if (i<0){
            throw new RuntimeException("更新失败");
        }
        //判断在阅读记录表中是否已经存在阅读记录
        OfNoticeRead ofNoticeRead = noticeReadMapper.selectOne(new LambdaQueryWrapper<OfNoticeRead>()
                .eq(OfNoticeRead::getNoticeId, id)
                .eq(OfNoticeRead::getUserId, userId)
        );
        if (ofNoticeRead == null){
            OfNoticeRead NoticeRead = new OfNoticeRead();
            NoticeRead.setNoticeId(id);
            NoticeRead.setUserId(userId);
            NoticeRead.setReadTime(LocalDateTime.now());
            int insert = noticeReadMapper.insert(NoticeRead);
            if (insert<0){
                throw new RuntimeException("插入失败");
            }
        }
        return BeanUtil.copyProperties(ofNotice, NoticeDTO.class);
    }

    @Override
    public void publishNotice(AUNoticeDTO noticeDTO, SysUser user) {
        OfNotice ofNotice = new OfNotice();
        BeanUtil.copyProperties(noticeDTO, ofNotice);
        ofNotice.setPublisherId(user.getId());
        ofNotice.setPublisherName(user.getNickname());
        if (noticeDTO.getPublishStatus()==1){
            ofNotice.setPublishTime(LocalDateTime.now());
            ofNotice.setViewCount(0);
        }
        int insert = noticeMapper.insert(ofNotice);
        if (insert<0){
            throw new RuntimeException("插入失败");
        }
    }

    @Override
    public void updateNotice(AUNoticeDTO noticeDTO) {
        Long noticeDTOId = noticeDTO.getId();
        OfNotice ofNotice = noticeMapper.selectById(noticeDTOId);
        if (ofNotice==null){
            throw new RuntimeException("通知公告不存在");
        }
        BeanUtils.copyProperties(noticeDTO, ofNotice);
        if (noticeDTO.getPublishStatus()==1) ofNotice.setPublishTime(LocalDateTime.now());
        noticeMapper.updateById(ofNotice);
    }
}
