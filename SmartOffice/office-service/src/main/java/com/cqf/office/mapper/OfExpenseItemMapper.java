package com.cqf.office.mapper;

import com.cqf.office.model.po.OfExpenseItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 报销明细表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-04-24
 */
public interface OfExpenseItemMapper extends BaseMapper<OfExpenseItem> {

    boolean savebatch(List<OfExpenseItem> list);
}
