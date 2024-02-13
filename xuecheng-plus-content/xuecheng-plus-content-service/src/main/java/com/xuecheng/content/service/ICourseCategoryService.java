package com.xuecheng.content.service;

import com.xuecheng.model.dto.CourseCategoryTreeDto;
import com.xuecheng.model.po.CourseCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
public interface ICourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryTreeDto> queryTreeNodes(String root);

}
