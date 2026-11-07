package com.aicomic.api.dto.comic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MediaQuoteDTO
 * @since 2022/7/18 17:43
 * @version 1.0.0
 **/
@ApiModel("素材被引用情况")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaQuoteDTO {
    @ApiModelProperty("素材id")
    private Long mediaId;
    @ApiModelProperty("引用数")
    private Integer quoteNum;
}
