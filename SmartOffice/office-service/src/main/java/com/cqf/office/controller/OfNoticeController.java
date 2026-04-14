package com.cqf.office.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.po.SysUser;
import com.cqf.common.result.Result;
import com.cqf.office.model.dto.AUNoticeDTO;
import com.cqf.office.model.dto.NoticeDTO;
import com.cqf.office.model.dto.NoticeQueryParam;
import com.cqf.office.model.po.OfNotice;
import com.cqf.office.model.po.OfNoticeRead;
import com.cqf.office.model.vo.NoticeVo;
import com.cqf.office.service.IOfNoticeReadService;
import com.cqf.office.service.IOfNoticeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 通知公告表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-04-14
 */
@RestController
@RequestMapping("/office/notice")
@RequiredArgsConstructor
public class OfNoticeController {
    private final IOfNoticeService noticeService;
    private final IOfNoticeReadService noticeReadService;
    private final AuthClient authClient;
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('notice:list')")
    public Result<PageResult<NoticeVo>> list(NoticeQueryParam  param) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        //查找当前用户已查看的公告id
        List<OfNoticeRead> noticeReadList = noticeReadService.lambdaQuery()
                .eq(OfNoticeRead::getUserId, userId)
                .list();
        Map<Long, OfNoticeRead> noticeReadMap = noticeReadList.stream().collect(Collectors.toMap(OfNoticeRead::getNoticeId, item -> item));
        Integer publishStatus = param.getPublishStatus();
        //查询所有通知公告
        Page<OfNotice> page = noticeService.lambdaQuery()
                .like(StringUtils.isNotBlank(param.getTitle()), OfNotice::getTitle, param.getTitle())
                .eq(OfNotice::getPublishStatus, publishStatus)
                .eq(publishStatus==0,OfNotice::getPublisherId, userId)
                .orderBy(true,true,OfNotice::getPriority)
                .orderBy(true, false, OfNotice::getPublishTime)
                .page(new Page<>(param.getCurrent(), param.getSize()));
        PageResult<NoticeVo> noticeVoPageResult = new PageResult<>();
        noticeVoPageResult.setTotal((int) page.getTotal());
        noticeVoPageResult.setPages((int) page.getPages());
        noticeVoPageResult.setSize((int) page.getSize());
        noticeVoPageResult.setCurrent((int) page.getCurrent());
        noticeVoPageResult.setRecords(page.getRecords().stream().map(ofNotice -> {
            NoticeVo noticeVo = BeanUtil.copyProperties(ofNotice, NoticeVo.class);
            noticeVo.setIsRead(noticeReadMap.containsKey(ofNotice.getId()));
            return noticeVo;
        }).toList());
        return Result.success(noticeVoPageResult);
    }
    @GetMapping("/{id}")
    public Result<NoticeDTO> get(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        NoticeDTO noticeDTO =noticeService.readNotice(id,userId);
        return Result.success(noticeDTO);
    }
    @PostMapping()
    @PreAuthorize("hasAuthority('notice:add')")
    public Result add(@RequestBody AUNoticeDTO noticeDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = authClient.getUser(username);
        noticeService.publishNotice(noticeDTO,user);
        return Result.success();
    }
    @PutMapping()
    @PreAuthorize("hasAuthority('notice:edit')")
    public Result update(@RequestBody AUNoticeDTO noticeDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = authClient.getUser(username);
        noticeService.updateNotice(noticeDTO);
        return Result.success();
    }


}
