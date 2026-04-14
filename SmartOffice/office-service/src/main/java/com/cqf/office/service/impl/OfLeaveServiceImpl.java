package com.cqf.office.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cqf.common.domain.po.SysUser;
import com.cqf.common.domain.vo.DeptVo;
import com.cqf.common.enums.LeaveEnum;
import com.cqf.office.enums.LeaveDaysEnum;
import com.cqf.office.model.dto.AddLeaveDTO;
import com.cqf.office.model.po.OfLeave;
import com.cqf.office.mapper.OfLeaveMapper;
import com.cqf.office.model.vo.LeaveVo;
import com.cqf.office.service.IOfLeaveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 请假申请表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-13
 */
@Service
@RequiredArgsConstructor
public class OfLeaveServiceImpl extends ServiceImpl<OfLeaveMapper, OfLeave> implements IOfLeaveService {
    private final OfLeaveMapper leaveMapper;

    @Override
    public Map<String, Integer> getBalance(String username) {
        //获取当前的年份和明年的年份
        int year = LocalDate.now().getYear();
        int nextYear = year + 1;
        //查询开始日期和结束日期在年份内的数据
        List<Map<String, Object>>  result = leaveMapper.selectLeaveDurationByType(
                LocalDate.of(year, 1, 1).toString(),
                LocalDate.of(nextYear, 1, 1).toString(),
                username
        );
        //将结果转换为Map
        Map<String, Integer> resultCollect = result.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("leaveType"),
                        map -> ((BigDecimal) map.get("duration")).intValue()
                ));
        //计算剩余天数
        calculateDays(resultCollect);

        return resultCollect;
    }

    @Override
    public LeaveVo addLeave(AddLeaveDTO leaveDTO, SysUser  user, DeptVo deptVo) {
        Long userId = user.getId();
        String username = user.getUsername();
        Long deptId = user.getDeptId();
        String deptName = deptVo.getName();
        String leaveNo = "LV" + System.currentTimeMillis() + userId;
        String leaveType = leaveDTO.getLeaveType();
        long days = ChronoUnit.DAYS.between(leaveDTO.getStartDate(), leaveDTO.getEndDate()) + 1;
        Map<String, Integer> balance = getBalance(username);
        Integer balanceDays = balance.get(leaveType);
        if (balanceDays != null){
            if (balanceDays < days) {
                throw new RuntimeException(leaveType+"剩余天数不足");
            }
        }

        //封装数据
        OfLeave ofLeave = new OfLeave();
        ofLeave.setLeaveNo(leaveNo);
        ofLeave.setLeaveType(leaveType);
        ofLeave.setStartDate(leaveDTO.getStartDate());
        ofLeave.setEndDate(leaveDTO.getEndDate());
        ofLeave.setReason(leaveDTO.getReason());
        ofLeave.setDuration(BigDecimal.valueOf(days));
        ofLeave.setUserId(userId);
        ofLeave.setUserName(username);
        ofLeave.setDeptId(deptId);
        ofLeave.setDeptName(deptName);
        ofLeave.setStatus(LeaveEnum.PENDING.getCode());
        //插入数据
        leaveMapper.insert(ofLeave);
        LeaveVo leaveVo = BeanUtil.copyProperties(ofLeave, LeaveVo.class);
        leaveVo.setDays(BigDecimal.valueOf(days));
        return leaveVo;
    }

    private static void calculateDays(Map<String, Integer> result) {
        Integer yearLeave = result.get("年假");
        if (yearLeave != null) {
            result.put("年假", LeaveDaysEnum.YEARLY_LEAVE.getDays() - yearLeave);
        }else {
            result.put("年假", LeaveDaysEnum.YEARLY_LEAVE.getDays());
        }
        Integer sickLeave = result.get("病假");
        if (sickLeave != null) {
            result.put("病假", LeaveDaysEnum.SICK_LEAVE.getDays() - sickLeave);
        }else{
            result.put("病假", LeaveDaysEnum.SICK_LEAVE.getDays());
        }
        Integer maternityLeave = result.get("产假");
        if (maternityLeave != null) {
            result.put("产假", LeaveDaysEnum.MATERNITY_LEAVE.getDays() - maternityLeave);
        }else {
            result.put("产假", LeaveDaysEnum.MATERNITY_LEAVE.getDays());
        }
        Integer marriageLeave = result.get("婚假");
        if (marriageLeave != null) {
            result.put("婚假", LeaveDaysEnum.MARRIAGE_LEAVE.getDays() - marriageLeave);
        }else {
            result.put("婚假", LeaveDaysEnum.MARRIAGE_LEAVE.getDays());
        }
    }
}
