package com.cqf.auth.service;

import com.cqf.auth.model.po.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqf.auth.model.vo.SysMenuVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author author
 * @since 2026-03-26
 */
public interface ISysMenuService extends IService<SysMenu> {

    List<SysMenuVo> getMenuTree();

    List<SysMenuVo> getUserRoutes();
}
