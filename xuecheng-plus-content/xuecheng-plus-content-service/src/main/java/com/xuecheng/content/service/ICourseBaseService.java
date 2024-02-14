package com.xuecheng.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.model.dto.AddCourseDto;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.EditCourseDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
public interface ICourseBaseService extends IService<CourseBase> {

    PageResult<CourseBase> listByQuery(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    CourseBase add(Long companyId, AddCourseDto addCourseDto);

    CourseBaseInfoDto getCourseBaseById(Long courseId);

    CourseBaseInfoDto updateCourseBaseById(Long companyId, EditCourseDto editCourseDto);
}
