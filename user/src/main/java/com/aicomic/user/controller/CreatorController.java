package com.aicomic.user.controller;


import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.user.domain.query.UserPageQuery;
import com.aicomic.user.domain.vo.CreatorPageVO;
import com.aicomic.user.service.ICreatorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 创作者详情表 前端控制器
 * </p>
 *
 * @since 2022-07-12
 */
@RestController
@RequestMapping("/creators")
@Api(tags = "用户管理接口")
public class CreatorController {

    @Autowired
    private ICreatorService creatorService;

    @GetMapping("/page")
    @ApiOperation("分页查询创作者信息")
    public PageDTO<CreatorPageVO> queryTeacherPage(UserPageQuery pageQuery){
        return creatorService.queryTeacherPage(pageQuery);
    }
}
