package com.cqf.auth.controller;


import com.cqf.auth.model.po.SysMenu;
import com.cqf.auth.model.po.SysRole;
import com.cqf.auth.model.po.SysRoleMenu;
import com.cqf.auth.model.vo.SysMenuVo;
import com.cqf.auth.service.ISysMenuService;
import com.cqf.auth.service.ISysRoleMenuService;
import com.cqf.auth.service.ISysRoleService;
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
    private final ISysRoleMenuService sysRoleMenuService;
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
        SysRoleMenu sysRoleMenu = new SysRoleMenu();
        sysRoleMenu.setMenuId(menu.getId());
        sysRoleMenu.setRoleId(1L);
        sysRoleMenuService.save(sysRoleMenu);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.updateById(menu);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuService.removeById(id);
        return Result.success();
    }
    /**
     * 获取菜单路由
     * @return
     */
    @GetMapping("routes")
    public Result<List<SysMenuVo>>  routes(){
        List<SysMenuVo> routes =sysMenuService.getSysMenuVosByRoleIds(null);
        return Result.success(routes);
    }

}
