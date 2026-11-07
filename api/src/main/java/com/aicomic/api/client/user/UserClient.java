package com.aicomic.api.client.user;


import com.aicomic.api.client.user.fallback.UserClientFallback;
import com.aicomic.api.dto.user.LoginFormDTO;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.domain.dto.LoginUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "user-service", fallbackFactory = UserClientFallback.class)
public interface UserClient {

    /**
     * 根据手机号查询用户id
     * @param phone 手机号
     * @return 用户id
     */
    @GetMapping("/users/ids")
    Long exchangeUserIdWithPhone(@RequestParam("phone") String phone);

    /**
     * 登录接口
     * @param loginDTO 登录信息
     * @param isAdmin 是否是管理员
     * @return 用户详情
     */
    @PostMapping("/users/detail/{isAdmin}")
    LoginUserDTO queryUserDetail(@RequestBody LoginFormDTO loginDTO, @PathVariable("isAdmin") boolean isAdmin);

    /**
     * 查询用户类型
     * @param id 用户id
     * @return 用户类型，0-普通观众，1-创作者，2-其他管理员
     */
    @GetMapping("/users/{id}/type")
    Integer queryUserType(@PathVariable("id") Long id);

    /**
     * <h1>根据id批量查询用户信息</h1>
     * @param ids 用户id集合
     * @return 用户集合
     */
    @GetMapping("/users/list")
    List<UserDTO> queryUserByIds(@RequestParam("ids") Iterable<Long> ids);


    /**
     * 根据id查询单个观众信息
     * @param id 用户id
     * @return 观众
     */
    @GetMapping("/users/{id}")
    UserDTO queryUserById(@PathVariable("id") Long id);
}
