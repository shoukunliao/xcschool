package com.xuecheng.content.service;

import com.xuecheng.model.po.CourseTeacher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
public interface ICourseTeacherService extends IService<CourseTeacher> {

    List<CourseTeacher> listById(Long courseId);

    void add(CourseTeacher courseTeacher);

    void deleteBycIdAndtId(Long courseId, Long teacherId);

    CourseTeacher insertOrUpdate(CourseTeacher courseTeacher);
}
