package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.model.dto.CourseCategoryTreeDto;
import com.xuecheng.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    List<CourseCategoryTreeDto> selectTreeNodes(String root);
}
