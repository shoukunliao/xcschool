package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.service.ICourseBaseService;
import com.xuecheng.model.dto.AddCourseDto;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.EditCourseDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Autowired
    ICourseBaseService courseBaseService;

    @ApiOperation("课程查询接口")
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseService.listByQuery(pageParams, queryCourseParamsDto);
    }
    @ApiOperation("添加课程接口")
    @PostMapping
    public CourseBase save(Long companyId,@RequestBody @Validated({ValidationGroups.Inster.class}) AddCourseDto addCourseDto) {
        companyId = 1232141425L;
        return courseBaseService.add(companyId,addCourseDto);
    }

    @GetMapping("/{courseId}")
    @ApiOperation("根据id查询课程接口")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
        return courseBaseService.getCourseBaseById(courseId);

    }

    @PutMapping
    @ApiOperation("根据id修改课程接口")
    public CourseBaseInfoDto updateCourseBaseById(Long companyId,@RequestBody EditCourseDto editCourseDto){
        companyId = 1232141425L;
        return courseBaseService.updateCourseBaseById(companyId,editCourseDto);

    }

}
