package com.cqf.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.auth.mapper.SysRoleMapper;
import com.cqf.auth.mapper.SysUserMapper;
import com.cqf.auth.model.po.SysMenu;
import com.cqf.auth.mapper.SysMenuMapper;
import com.cqf.auth.model.po.SysRole;
import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.model.vo.SysMenuVo;
import com.cqf.auth.service.ISysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqf.common.result.LoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-03-26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {
    private final SysMenuMapper mapper;
    private final RedisTemplate redisTemplate;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper menuMapper;
    @Override
    public List<SysMenuVo> getMenuTree() {
        List<SysMenu> menuList = mapper.selectList(null);
        // 获取所有子级菜单
        List<SysMenu> sysMenus=menuList.stream().filter(menu -> menu.getParentId() != 0).toList();
        Map<Long, List<SysMenuVo>> childrenMap = sysMenus.stream().map(menu -> {
            SysMenuVo sysMenuVo = BeanUtil.copyProperties(menu, SysMenuVo.class);
            SysMenuVo.MenuMeta menuMeta = new SysMenuVo.MenuMeta();
            menuMeta.setTitle(menu.getName());
            menuMeta.setIcon(menu.getIcon());
            sysMenuVo.setMeta(menuMeta);
            return sysMenuVo;
        }).collect(Collectors.groupingBy(SysMenuVo::getParentId));
        // 获取所有父级菜单
        List<SysMenu> parentMenus = menuList.stream().filter(menu -> menu.getParentId() == 0).toList();
        ArrayList<SysMenuVo> sysMenuVos = new ArrayList<>();
        parentMenus.forEach(parentMenu -> {
            SysMenuVo sysMenuVo = BeanUtil.copyProperties(parentMenu, SysMenuVo.class);
            SysMenuVo.MenuMeta menuMeta = new SysMenuVo.MenuMeta();
            menuMeta.setTitle(parentMenu.getName());
            menuMeta.setIcon(parentMenu.getIcon());
            sysMenuVo.setMeta(menuMeta);
            List<SysMenuVo> children = childrenMap.get(parentMenu.getId());
            sysMenuVo.setChildren(children);
            if (children != null && !children.isEmpty())sysMenuVo.setRedirect(children.get(0).getPath());
            sysMenuVos.add(sysMenuVo);
        });

        return sysMenuVos;
    }

    @Override
    public List<SysMenuVo> getSysMenuVosByRoleIds(List<Long> roleIds) {
        // 获取菜单树
        List<SysMenuVo> menuTree = getMenuTree();
        if (roleIds.size()==0||roleIds==null) {
            // 获取用户角色
            SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, SecurityContextHolder.getContext().getAuthentication().getName()));
            // 获取用户信息
            Object user = redisTemplate.opsForValue().get("smart:user:" + sysUser.getId());
            LoginResult loginResult=null;
            try {
                JSON userJson = JSONUtil.parse(user);
                loginResult = JSONUtil.toBean((JSONObject) userJson, LoginResult.class);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("类型转换失败");
            }
            String[] roles = loginResult.getRoles();

        List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getCode, roles)
                .select(SysRole::getId));
        roleIds = sysRoles.stream().map(SysRole::getId).toList();
        }
        List<SysMenu> menuByRoleIds = menuMapper.getMenuByRoleIds(roleIds);
        Set<Long> ids = menuByRoleIds.stream().map(SysMenu::getId).collect(Collectors.toSet());
        menuTree.removeIf(menu -> !ids.contains(menu.getId()));
        return menuTree;
    }
}
