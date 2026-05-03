package com.cqf.auth.service.impl;

import ch.qos.logback.classic.spi.EventArgUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqf.auth.mapper.SysUserMapper;
import com.cqf.auth.mapper.SysUserRoleMapper;
import com.cqf.auth.model.dto.LoginDTO;
import com.cqf.auth.model.po.SysUser;
import com.cqf.auth.model.po.SysUserRole;
import com.cqf.auth.service.AuthService;
import com.cqf.auth.service.WxAuthService;
import com.cqf.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service("wx_authservice")
@Slf4j
@RequiredArgsConstructor
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserMapper userMapper;
    private final RestTemplate restTemplate;
    @Value("${wx.appid}")
    private String appid;
    @Value("${wx.secret}")
    private String secret;

    @Override
    public SysUser execute(LoginDTO authParamsDto) {
        String username = authParamsDto.getUsername();
        SysUser sysUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (sysUser!=null){
            return sysUser;
        }
        return null;
    }

    @Override
    public SysUser wxAuth(String code) {
        //通过code获取access_token
        Map<String,String> accessTokenMap=getAccessToken(code);
        if (accessTokenMap==null){
            return null;
        }
        String accessToken = accessTokenMap.get("access_token");
        String openid = accessTokenMap.get("openid");

        //通过access_token获取用户个人信息
        Map<String,String> userInfoMap=getUserInfo(accessToken,openid);
        if (userInfoMap==null){
            return null;
        }
        //保存用户信息
        SysUser user=saveUser(userInfoMap);
        if (user==null){
            throw new BusinessException("用户信息保存失败");
        }
        return user;
    }

    private SysUser saveUser(Map<String, String> userInfoMap) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, userInfoMap.get("openid")));
        if (user!=null){
            log.info("用户已存在,直接返回:{}", user);
            return user;
        }
        //保存user表
        log.info("保存用户信息:{}", userInfoMap);
        SysUser sysUser = new SysUser();
        sysUser.setUsername(userInfoMap.get("openid"));
        sysUser.setPassword(userInfoMap.get("openid"));
        sysUser.setNickname(userInfoMap.get("nickname"));
        sysUser.setAvatar(userInfoMap.get("headimgurl"));
        sysUser.setDeptId(4L);//人力资源部
        sysUser.setStatus(1);//启用
        sysUser.setLoginDate(LocalDateTime.now());
        SysUser sysUser1 = userMapper.insert(sysUser) > 0 ? sysUser : null;
        //保存用户角色表
        Long id = sysUser1.getId();
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(id);
        sysUserRole.setRoleId(4L);//普通员工
        userRoleMapper.insert(sysUserRole);

        return sysUser1;
    }

    /**
     * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
     * {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     *
     * }
     * @param access_token
     * @param openid
     * @return
     */
    private Map<String, String> getUserInfo(String access_token, String openid){
        String url="https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String userInfo_url=String.format(url,access_token,openid);
        ResponseEntity<String> response = restTemplate.exchange(userInfo_url, HttpMethod.GET, null, String.class);
        //防止乱码进行转码
        String result = new String(response.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        log.info("调用微信接口申请用户信息: 响应值:{}", result);
        return JSONUtil.toBean(result, Map.class);
    }

    /**
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     * @param code
     * @return
     */
    private Map<String, String> getAccessToken(String code) {
        String url="https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String access_token_url=String.format(url,appid,secret,code);
        ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.GET, null, String.class);
        //防止乱码进行转码
        String result = new String(response.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        return JSONUtil.toBean(result, Map.class);
    }
}
