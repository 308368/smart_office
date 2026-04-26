package com.cqf.office.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.result.Result;
import com.cqf.office.model.dto.AddExpenseDTO;
import com.cqf.office.model.dto.ExpenseQueryParam;
import com.cqf.office.model.po.OfExpense;
import com.cqf.office.model.vo.ExpenseDetailVo;
import com.cqf.office.model.vo.ExpenseVo;
import com.cqf.office.service.IOfExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 报销申请表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/office/expense")
@RequiredArgsConstructor
public class OfExpenseController {
    private final IOfExpenseService expenseService;
    private final AuthClient authClient;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('expense:add')")
    public Result<ExpenseDetailVo> create(
            @RequestParam("expenseType") String expenseType,
            @RequestParam("totalAmount") String totalAmount,
            @RequestParam("description") String description,
            @RequestParam("itemsJson") String itemsJson,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws Exception {
        AddExpenseDTO dto = new AddExpenseDTO();
        dto.setExpenseType(expenseType);
        dto.setTotalAmount(new java.math.BigDecimal(totalAmount));
        dto.setDescription(description);
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.findAndRegisterModules();
        dto.setItems(objectMapper.readValue(itemsJson, new com.fasterxml.jackson.core.type.TypeReference<List<AddExpenseDTO.ExpenseItemDTO>>() {}));
        return Result.success(expenseService.createExpense(dto, files));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('expense:list')")
    public Result<PageResult<ExpenseVo>> list(ExpenseQueryParam param) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = authClient.getUserId(username);
        Page<OfExpense> expensePage = expenseService.lambdaQuery()
                .eq(param.getStatus() != null, OfExpense::getStatus, param.getStatus())
                .eq(param.getExpenseType() != null, OfExpense::getExpenseType, param.getExpenseType())
                .eq(OfExpense::getUserId,userId)
                .ge(param.getStartDate() != null && param.getEndDate() != null, OfExpense::getCreateTime, param.getStartDate())
                .le(param.getStartDate() != null && param.getEndDate() != null, OfExpense::getCreateTime, param.getEndDate())
                .orderByDesc(OfExpense::getCreateTime)
                .page(new Page<>(param.getCurrent(), param.getSize()));
        PageResult<ExpenseVo> pageResult = new PageResult<>();
        pageResult.setRecords(expensePage.getRecords().stream().map(expense -> {
            ExpenseVo expenseVo = new ExpenseVo();
            BeanUtil.copyProperties(expense, expenseVo);
            expenseVo.setStatus(expense.getStatus().getCode());
            expenseVo.setStatusText(expense.getStatus().getLabel());
            return expenseVo;
        }).toList());
        pageResult.setTotal((int) expensePage.getTotal());
        pageResult.setPages((int) expensePage.getPages());
        pageResult.setSize((int) expensePage.getSize());
        pageResult.setCurrent((int) expensePage.getCurrent());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<ExpenseDetailVo> detail(@PathVariable Long id) {
        return Result.success(expenseService.getExpenseDetail(id));
    }

    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) {
        expenseService.cancel(id);
        return Result.success();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('expense:approve')")
    public Result<PageResult<ExpenseVo>> pending(ExpenseQueryParam param) {
        return Result.success(expenseService.getPendingList(param));
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('expense:approve')")
    public Result approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Boolean approved = (Boolean) body.get("approved");
        String comment = (String) body.get("comment");
        expenseService.approve(id, approved, comment);
        return Result.success();
    }
}
