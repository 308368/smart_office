package com.cqf.office.mapper;

import com.cqf.office.model.po.OfLeave;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 请假申请表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-04-13
 */
public interface OfLeaveMapper extends BaseMapper<OfLeave> {

    List<Map<String, Object>>  selectLeaveDurationByType(String start, String end, String username);
}
