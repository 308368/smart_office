package com.cqf.office.service;

import com.cqf.common.domain.po.SysUser;
import com.cqf.common.domain.vo.DeptVo;
import com.cqf.office.model.dto.AddLeaveDTO;
import com.cqf.office.model.po.OfLeave;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.office.model.vo.LeaveVo;

import java.util.Map;

/**
 * <p>
 * 请假申请表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-13
 */
public interface IOfLeaveService extends IService<OfLeave> {

    Map<String, Integer> getBalance(String username);

    LeaveVo addLeave(AddLeaveDTO leaveDTO, SysUser user, DeptVo deptVo);
}
