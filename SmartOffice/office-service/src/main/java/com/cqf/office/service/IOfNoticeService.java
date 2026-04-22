package com.cqf.office.service;

import com.cqf.common.domain.po.SysUser;
import com.cqf.office.model.dto.AUNoticeDTO;
import com.cqf.office.model.dto.NoticeDTO;
import com.cqf.office.model.po.OfNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.office.model.vo.NoticeHomeVo;

import java.util.List;

/**
 * <p>
 * 通知公告表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-14
 */
public interface IOfNoticeService extends IService<OfNotice> {

    NoticeDTO readNotice(Long id,Long userId);

    void publishNotice(AUNoticeDTO noticeDTO, SysUser user);

    void updateNotice(AUNoticeDTO noticeDTO);

    List<NoticeHomeVo> home(Long userId);
}
