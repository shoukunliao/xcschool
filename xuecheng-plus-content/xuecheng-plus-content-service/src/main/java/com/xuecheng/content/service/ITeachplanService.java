package com.xuecheng.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.model.dto.TeachPlanDto;
import com.xuecheng.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
public interface ITeachplanService extends IService<Teachplan> {

    List<TeachPlanDto> getTeachPlanTreeByCourseId(Long courseId);

    TeachPlanDto insertOrUpdate(TeachPlanDto teachPlanDto);

    void deleteById(Long id);
}
