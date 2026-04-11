package com.cqf.api.client;

import com.cqf.api.client.fallback.AuthClientFallbackFactory;
import com.cqf.api.config.DefaultFeignConfig;
import com.cqf.common.domain.po.SysUser;
import com.cqf.common.domain.vo.DeptVo;
import com.cqf.common.domain.vo.UserVo;
import com.cqf.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "auth-service",fallbackFactory = AuthClientFallbackFactory.class, configuration = DefaultFeignConfig.class)
public interface AuthClient {
    @GetMapping("/system/user/userInfo/{username}")
    Long getUserId(@PathVariable String username);
    @GetMapping("/system/user/user/{username}")
    SysUser getUser(@PathVariable String username);
    @GetMapping("/system/dept/list")
    Result<List<DeptVo>> list();
    @GetMapping("/system/user/{id}")
    Result<UserVo> getById(@PathVariable Long id);
}
