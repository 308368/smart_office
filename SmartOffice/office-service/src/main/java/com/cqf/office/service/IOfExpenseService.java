package com.cqf.office.service;

import com.cqf.common.domain.PageResult;
import com.cqf.office.model.dto.AddExpenseDTO;
import com.cqf.office.model.dto.ExpenseQueryParam;
import com.cqf.office.model.po.OfExpense;
import com.cqf.office.model.po.OfExpenseItem;
import com.cqf.office.model.vo.ExpenseDetailVo;
import com.cqf.office.model.vo.ExpenseVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 报销申请表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-24
 */
public interface IOfExpenseService extends IService<OfExpense> {

    /**
     * 创建报销申请
     */
    ExpenseDetailVo createExpense(AddExpenseDTO dto, MultipartFile[] files);

    /**
     * 获取报销详情
     */
    ExpenseDetailVo getExpenseDetail(Long id);

    /**
     * 取消报销
     */
    void cancel(Long id);

    /**
     * 获取待审批列表
     */
    PageResult<ExpenseVo> getPendingList(ExpenseQueryParam param);

    /**
     * 审批报销
     */
    void approve(Long id, Boolean approved, String comment);

    List<OfExpenseItem> addExpenseItemToDb(AddExpenseDTO dto, OfExpense ofExpense, ArrayList<String> filesUrl);

    OfExpense addExpenseToDb(AddExpenseDTO dto);
}
