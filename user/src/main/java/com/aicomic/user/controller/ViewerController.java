package com.aicomic.user.controller;


import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.user.domain.dto.ViewerFormDTO;
import com.aicomic.user.domain.query.UserPageQuery;
import com.aicomic.user.domain.vo.ViewerPageVo;
import com.aicomic.user.service.IViewerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 观众详情表 前端控制器
 * </p>
 *
 * @since 2022-07-12
 */
@RestController
@RequestMapping("/viewers")
@Api(tags = "用户管理接口")
public class ViewerController {

    @Autowired
    private IViewerService viewerService;

    @ApiOperation("分页查询观众信息")
    @GetMapping("/page")
    public PageDTO<ViewerPageVo> queryStudentPage(UserPageQuery pageQuery){
        return viewerService.queryStudentPage(pageQuery);
    }

    @ApiOperation("观众注册")
    @PostMapping("/register")
    public void registerStudent(@RequestBody ViewerFormDTO viewerFormDTO) {
        viewerService.saveStudent(viewerFormDTO);
    }

    @ApiOperation("修改观众密码")
    @PutMapping("/password")
    public void updateMyPassword(@RequestBody ViewerFormDTO viewerFormDTO) {
        viewerService.updateMyPassword(viewerFormDTO);
    }
}
