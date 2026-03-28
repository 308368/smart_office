package com.cqf.auth.model.vo;

import com.cqf.auth.model.po.SysMenu;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单 VO - 对应 /system/menu/list 和 /system/menu/routes 接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class SysMenuVo extends SysMenu {

    /**
     * 子菜单
     */
    private List<SysMenuVo> children;

    /**
     * 默认重定向路径（用于 routes 接口）
     */
    private String redirect;

    /**
     * 菜单元信息（用于 routes 接口）
     */
    private MenuMeta meta;

    /**
     * 菜单元信息
     */
    @Data
    public static class MenuMeta {
        /**
         * 菜单标题
         */
        private String title;

        /**
         * 图标
         */
        private String icon;

        /**
         * 允许的角色
         */
        private List<String> roles;
    }
}
