package com.aicomic.media.domain.query;

import com.aicomic.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "素材搜索条件")
public class MediaQuery extends PageQuery {
    @ApiModelProperty("素材名称关键字")
    private String name;
}
