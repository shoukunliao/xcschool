package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class PageParams {

    @ApiModelProperty("当前页面")
    private Long pageNo = 1L;
    @ApiModelProperty("页面大小")
    private Long pageSize = 10L;
}
