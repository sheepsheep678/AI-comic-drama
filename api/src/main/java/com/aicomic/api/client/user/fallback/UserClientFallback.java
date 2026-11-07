package com.aicomic.api.client.user.fallback;

import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.user.LoginFormDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.domain.dto.LoginUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collections;
import java.util.List;

@Slf4j
public class UserClientFallback implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        log.error("查询用户服务出现异常", cause);
        return new UserClient() {
            @Override
            public Long exchangeUserIdWithPhone(String phone) {
                return null;
            }

            @Override
            public LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isAdmin) {
                return null;
            }

            @Override
            public Integer queryUserType(Long id) {
                return null;
            }

            @Override
            public List<UserDTO> queryUserByIds(Iterable<Long> ids) {
                return Collections.emptyList();
            }

            @Override
            public UserDTO queryUserById(Long id) {
                return null;
            }
        };
    }
}
