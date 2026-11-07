package com.aicomic.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.api.dto.user.LoginFormDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.domain.dto.LoginUserDTO;
import com.aicomic.user.domain.dto.UserFormDTO;
import com.aicomic.user.domain.po.User;
import com.aicomic.user.domain.vo.UserDetailVO;

/**
 * <p>
 * 观众用户表 服务类
 * </p>
 *
 * @since 2022-06-28
 */
public interface IUserService extends IService<User> {
    LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isAdmin);

    void resetPassword(Long userId);

    UserDetailVO myInfo();

    void addUserByPhone(User user, String code);

    void updatePasswordByPhone(String cellPhone, String code, String password);

    Long saveUser(UserDTO userDTO);

    void updateUser(UserDTO userDTO);

    void updateUserWithPassword(UserFormDTO userDTO);
}
