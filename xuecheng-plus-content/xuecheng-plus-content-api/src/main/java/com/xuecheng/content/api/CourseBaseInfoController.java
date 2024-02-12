package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RequestMapping("/course")
public class CourseBaseInfoController {

    @ApiOperation("课程查询接口")
    @PostMapping("/list")
    public PageResult<CourseBase> list( PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){


        CourseBase courseBase = new CourseBase();
        courseBase.setName("测试名称");
        courseBase.setCreateDate(LocalDateTime.now());
        List<CourseBase> list = new ArrayList<>();
        list.add(courseBase);
        PageResult<CourseBase> re = new PageResult<>(list,1,1,10);


        return re;
    }
}
