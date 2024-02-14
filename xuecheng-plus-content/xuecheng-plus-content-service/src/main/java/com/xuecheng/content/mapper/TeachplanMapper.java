package com.xuecheng.content.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.model.dto.TeachPlanDto;
import com.xuecheng.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachPlanDto> getTeachPlanTreeByCourseId(Long courseId);

    @Select("select count(*) from teachplan where parentId = #{parentId} and course_id = #{courseId}")
    int getCountByParentId(@Param("parentId") Long parentId,@Param("courseId") Long courseId);

    List<TeachPlanDto> getTreeById(Long id);
}
