package com.aicomic.user.domain.query;

import com.aicomic.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "创作者分页查询条件")
public class UserPageQuery extends PageQuery {
    @ApiModelProperty(value = "账户状态")
    private Integer status;
    @ApiModelProperty(value = "创作者名称")
    private String name;
    @ApiModelProperty(value = "手机号码")
    private String phone;
}
