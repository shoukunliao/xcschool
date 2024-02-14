package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.model.po.CourseTeacher;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.service.ICourseTeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements ICourseTeacherService {

    /**
     * 根据id查询课程老师
     * @param courseId
     * @return
     */
    @Override
    public List<CourseTeacher> listById(Long courseId) {
        //查询
        LambdaQueryWrapper<CourseTeacher> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CourseTeacher::getCourseId,courseId);
        return list(lqw);
    }

    @Override
    public void add(CourseTeacher courseTeacher) {
        courseTeacher.setCreateDate(LocalDateTime.now());
        boolean save = save(courseTeacher);
        if (!save){
            throw new XueChengPlusException("添加失败");
        }
    }

    @Override
    public void deleteBycIdAndtId(Long courseId, Long teacherId) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("course_id",courseId);
        map.put("id",teacherId);
        boolean b = removeByMap(map);
        if (!b){
            throw new XueChengPlusException("删除失败");
        }
    }

    @Override
    public CourseTeacher insertOrUpdate(CourseTeacher courseTeacher) {
        //添加信息
        if (courseTeacher.getId() != null) {
            courseTeacher.setCreateDate(LocalDateTime.now());
        }
        boolean b = saveOrUpdate(courseTeacher);
        if (!b){
            throw new XueChengPlusException("操作失败");
        }
        return courseTeacher;
    }
}
