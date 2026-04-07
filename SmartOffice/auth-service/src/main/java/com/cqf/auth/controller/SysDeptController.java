package com.cqf.auth.controller;


import cn.hutool.core.bean.BeanUtil;
import com.cqf.auth.model.po.SysDept;
import com.cqf.auth.model.vo.DeptVo;
import com.cqf.auth.service.ISysDeptService;
import com.cqf.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 部门表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-29
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {
    private final ISysDeptService deptService;
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:dept:list')")
    public Result<List<DeptVo>> list() {
        List<SysDept> list = deptService.list();
        List<DeptVo> deptVos = BeanUtil.copyToList(list, DeptVo.class);
        return Result.success(deptVos);
    }

}
