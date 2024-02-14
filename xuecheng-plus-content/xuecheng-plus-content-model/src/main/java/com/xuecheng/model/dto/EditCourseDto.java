package com.xuecheng.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EditCourseDto extends AddCourseDto{
    @ApiModelProperty("课程id")
    private Long Id;
}
