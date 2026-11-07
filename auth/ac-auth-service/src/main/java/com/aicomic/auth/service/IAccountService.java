package com.aicomic.auth.service;

import com.aicomic.api.dto.user.LoginFormDTO;

/**
 * <p>
 * 账号表，平台内所有用户的账号、密码信息 服务类
 * </p>
 *
 * @since 2022-06-16
 */
public interface IAccountService{

    String login(LoginFormDTO loginFormDTO, boolean isAdmin);

    void logout();

    String refreshToken(String refreshToken);
}
