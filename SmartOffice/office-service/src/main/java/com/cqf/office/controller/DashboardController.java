package com.cqf.office.controller;

import com.cqf.common.result.Result;
import com.cqf.office.model.vo.DashboardVo;
import com.cqf.office.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    @GetMapping("/dashboard/stats")
    public Result<DashboardVo> dashboard(){
        DashboardVo dashboardVo =dashboardService.dashboard();
        return Result.success(dashboardVo);
    }
}
