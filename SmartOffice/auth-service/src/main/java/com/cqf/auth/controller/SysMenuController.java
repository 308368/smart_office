package com.cqf.auth.controller;


import com.cqf.auth.model.po.SysMenu;
import com.cqf.auth.model.vo.SysMenuVo;
import com.cqf.auth.service.ISysMenuService;
import com.cqf.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-03-26
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {
    private final ISysMenuService sysMenuService;
    @GetMapping("/list")
    public Result<List<SysMenuVo>> list() {
        List<SysMenuVo> menus = sysMenuService.getMenuTree();
        return Result.success(menus);
    }

    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.success(sysMenuService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<Void> add(@RequestBody SysMenu menu) {
        sysMenuService.save(menu);
        return Result.success(null);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.updateById(menu);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuService.removeById(id);
        return Result.success(null);
    }
    /**
     * 获取菜单路由
     * @return
     */
    @GetMapping("routes")
    public Result<List<SysMenuVo>>  routes(){
        List<SysMenuVo> routes =sysMenuService.getUserRoutes();
        return Result.success(routes);
    }

}
