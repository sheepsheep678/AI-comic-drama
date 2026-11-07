package com.aicomic.user.controller;


import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.user.domain.query.UserPageQuery;
import com.aicomic.user.domain.vo.AdminVO;
import com.aicomic.user.service.IAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 管理员详情表 前端控制器
 * </p>
 *
 * @since 2022-07-12
 */
@RestController
@RequestMapping("/admins")
@Api(tags = "用户管理接口")
public class AdminController {

    private final IAdminService adminService;

    public AdminController(IAdminService adminService) {
        this.adminService = adminService;
    }

    @ApiOperation("分页查询管理员信息")
    @GetMapping("page")
    public PageDTO<AdminVO> queryStaffPage(UserPageQuery pageQuery){
        return adminService.queryStaffPage(pageQuery);
    }
}
