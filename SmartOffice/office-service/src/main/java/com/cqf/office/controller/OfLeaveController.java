package com.cqf.office.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.PageResult;
import com.cqf.common.domain.QueryParam;
import com.cqf.common.domain.po.SysUser;
import com.cqf.common.domain.vo.DeptVo;
import com.cqf.common.enums.LeaveEnum;
import com.cqf.common.result.Result;
import com.cqf.office.model.dto.AddLeaveDTO;
import com.cqf.office.model.dto.ApproveDTO;
import com.cqf.office.model.dto.LeaveQueryParam;
import com.cqf.office.model.po.OfLeave;
import com.cqf.office.model.vo.LeavePendingVo;
import com.cqf.office.model.vo.LeaveVo;
import com.cqf.office.service.IOfLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 请假申请表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/office/leave")
@RequiredArgsConstructor
public class OfLeaveController {
    private final IOfLeaveService leaveService;
    private final AuthClient authClient;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('leave:list')")
    public Result<PageResult<LeaveVo>> list(LeaveQueryParam queryParam) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<OfLeave> ofLeavePage = leaveService.lambdaQuery()
                .eq(queryParam.getStatus() != null, OfLeave::getStatus, queryParam.getStatus())
                .eq(queryParam.getLeaveType() != null, OfLeave::getLeaveType, queryParam.getLeaveType())
                .eq(OfLeave::getUserName, username)
                .orderBy(true, false, OfLeave::getCreateTime)
                .page(new Page<>(queryParam.getCurrent(), queryParam.getSize()));
        PageResult<LeaveVo> leaveVoPageResult = new PageResult<>();
        leaveVoPageResult.setCurrent((int) ofLeavePage.getCurrent());
        leaveVoPageResult.setTotal((int) ofLeavePage.getTotal());
        leaveVoPageResult.setRecords(ofLeavePage.getRecords().stream().map(ofLeave -> {
            LeaveVo leaveVo = new LeaveVo();
            BeanUtil.copyProperties(ofLeave, leaveVo);
//            LocalDate startDate = ofLeave.getStartDate();
//            LocalDate endDate = ofLeave.getEndDate();
            //计算间隔天数
//            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            leaveVo.setDays(ofLeave.getDuration());
            return leaveVo;
        }).toList());
        return Result.success(leaveVoPageResult);
    }

    @GetMapping("/balance")
    public Result<Map<String, Integer>> balance() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Integer> days = leaveService.getBalance(username);
        return Result.success(days);
    }

    @PostMapping("/create")
    public Result<LeaveVo> create(@RequestBody AddLeaveDTO leaveDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = authClient.getUser(username);
        Result<List<DeptVo>> deptResult = authClient.list();
        List<DeptVo> deptVoList = deptResult.getData();
        DeptVo deptVo = deptVoList.stream().filter(dept -> dept.getId().equals(user.getDeptId())).findFirst().orElse(null);
        LeaveVo leaveVo = leaveService.addLeave(leaveDTO, user, deptVo);
        return Result.success(leaveVo);
    }

    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasAuthority('leave:cancel')")
    public Result cancel(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean cancel = leaveService.lambdaUpdate()
                .eq(OfLeave::getId, id)
                .eq(OfLeave::getUserName, username)
                .set(OfLeave::getStatus, LeaveEnum.CANCELLED.getCode())
                .update();
        if (cancel) {
            return Result.success("取消成功");
        }
        return Result.error("取消失败");
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('leave:approve')")
    public Result<PageResult<LeavePendingVo>> pending(QueryParam queryParam) {
        Page<OfLeave> ofLeavePage = leaveService.lambdaQuery()
                .eq(OfLeave::getStatus, LeaveEnum.PENDING.getCode())
                .page(new Page<>(queryParam.getCurrent(), queryParam.getSize()));
        PageResult<LeavePendingVo> leavePendingVoPageResult = new PageResult<>();
        leavePendingVoPageResult.setCurrent((int) ofLeavePage.getCurrent());
        leavePendingVoPageResult.setTotal((int) ofLeavePage.getTotal());
        leavePendingVoPageResult.setRecords(ofLeavePage.getRecords().stream().map(ofLeave -> {
            LeavePendingVo leavePendingVo = new LeavePendingVo();
            BeanUtil.copyProperties(ofLeave, leavePendingVo);
            return leavePendingVo;
        }).toList());
        return Result.success(leavePendingVoPageResult);
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('leave:approve')")
    public Result approve(@PathVariable Long id, @RequestBody ApproveDTO approveDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = authClient.getUser(username);
        Integer code = approveDTO.getApprove() ? LeaveEnum.APPROVED.getCode() : LeaveEnum.REJECTED.getCode();
        boolean approve = leaveService.lambdaUpdate()
                .eq(OfLeave::getId, id)
                .set(OfLeave::getStatus, code)
                .set(OfLeave::getApproveComment, approveDTO.getRemark())
                .set(OfLeave::getApproverId, user.getId())
                .set(OfLeave::getApproverName, user.getNickname())
                .set(OfLeave::getApproveTime, LocalDateTime.now())
                .update();
        if (approve) {
            return Result.success("审批成功");
        }
        return Result.error("审批失败");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('leave:list')")
    public Result<OfLeave> get(@PathVariable Long id) {
        OfLeave ofLeave = leaveService.getById(id);
        return Result.success(ofLeave);
    }
    @GetMapping("/pendingLeave")
    public Long pendingLeave(){
        return leaveService.lambdaQuery()
                .eq(OfLeave::getStatus, LeaveEnum.PENDING.getCode())
                .count();
    }

}
