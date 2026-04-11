package com.cqf.api.client.fallback;

import com.cqf.api.client.AuthClient;
import com.cqf.common.domain.po.SysUser;
import com.cqf.common.domain.vo.DeptVo;
import com.cqf.common.domain.vo.UserVo;
import com.cqf.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AuthClientFallbackFactory implements FallbackFactory<AuthClient> {
    @Override
    public AuthClient create(Throwable cause) {
        return new AuthClient() {
            @Override
            public Long getUserId(String username) {
                log.info("认证服务异常{}",cause.getMessage());
                log.error("getUserId error:{}", cause.getMessage());
                return -1L;
            }

            @Override
            public SysUser getUser(String username) {
                log.info("认证服务异常{}",cause.getMessage());
                log.error("getUser error:{}", cause.getMessage());
                return null;
            }

            @Override
            public Result<List<DeptVo>> list() {
                log.info("认证服务查询部门异常{}",cause.getMessage());
                log.error("list error:{}", cause.getMessage());
                return null;
            }

            @Override
            public Result<UserVo> getById(Long id) {
                log.info("认证服务查询用户异常{}",cause.getMessage());
                log.error("getById error:{}", cause.getMessage());
                return null;
            }
        };
    }
}
