package com.xuecheng.content.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.model.dto.TeachPlanDto;
import com.xuecheng.model.po.CourseBase;
import com.xuecheng.model.po.Teachplan;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.service.ITeachplanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.model.po.TeachplanMedia;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements ITeachplanService {
    /**
     * 查询课程计划树
     *
     * @param courseId 课程id
     * @return List<TeachPlanDto>
     */
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Override
    public List<TeachPlanDto> getTeachPlanTreeByCourseId(Long courseId) {
        //根据id查询课程
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            throw new XueChengPlusException("该课程不存在");
        }
        //查询计划树
        List<TeachPlanDto> list = this.getBaseMapper().getTeachPlanTreeByCourseId(courseId);


        return list;
    }

    @Override
    public TeachPlanDto insertOrUpdate(TeachPlanDto teachPlanDto) {
        //排序实现
        Long parentId = teachPlanDto.getParentid();
        Long courseId = teachPlanDto.getCourseId();
        if (teachPlanDto.getId() == null) {//新增

            //计算父节点有多少个子节点 m
            //orderby = m + 1
            //sql: select count(*) from teachplan where parentId = #{parentId} and course_id = #{courseId}

            int count = this.getBaseMapper().getCountByParentId(parentId, courseId);

            teachPlanDto.setOrderby(count + 1);
            teachPlanDto.setCreateDate(LocalDateTime.now());
        } else {
            teachPlanDto.setChangeDate(LocalDateTime.now());
        }

        Teachplan teachplan = new Teachplan();
        BeanUtils.copyProperties(teachPlanDto, teachplan);
        boolean b = saveOrUpdate(teachplan);
        if (!b) {
            String errorMessage = teachPlanDto.getId() == null ? "新增失败" : "更新失败";
            throw new XueChengPlusException(errorMessage);
        }
        //向meadia表中添加信息
        if (teachPlanDto.getId() == null) {
            TeachplanMedia teachplanMedia = new TeachplanMedia();
            teachplanMedia.setTeachplanId(teachplan.getId());
            teachplanMedia.setCreateDate(LocalDateTime.now());
            teachplanMedia.setCourseId(courseId);
            teachplanMedia.setMediaFilename("");
            teachplanMediaMapper.insert(teachplanMedia);
        }


        return teachPlanDto;
    }

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    /**
     * 删除课程计划
     *
     * @param id 计划id
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        //查询课程
        Teachplan teachplan = getById(id);
        if (teachplan == null) {
            throw new XueChengPlusException("该课程不存在");
        }
        //查询是否有子节点
        List<Teachplan> list = getChildTreeById(id,teachplan.getCourseId());
        if (list != null && list.size() != 0) {
            throw new XueChengPlusException("存在子目录无法删除！");
        }
        //删除节点
        boolean b = removeById(id);
        if (!b) {
            throw new XueChengPlusException("删除失败！");

        }
        //删除与之关联的media
        int i = teachplanMediaMapper.deleteById(id);

    }

    /**
     * 上下移动章节
     *
     * @param id
     */
    @Override
    public void moveById(Long id, String type) {

        //查询该计划
        Teachplan teachplan = getById(id);
        if (teachplan == null) {
            throw new XueChengPlusException("该课程存在");
        }
        //查询兄弟节点
        List<Teachplan> list = this.getBaseMapper().getChildTreeById(teachplan.getParentid(),teachplan.getCourseId());
        if (list.size() <= 1) {
            return;
        }
        int i = list.indexOf(teachplan);
        Teachplan TeachPlanDto;
        if ("movedown".equals(type)) {
            //向下移动
            if (i == list.size() - 1) return;
            else {
                 TeachPlanDto = list.get(i + 1);
            }
        } else {
            //向上移动
            if (i == 0) return;
            else {
                 TeachPlanDto = list.get(i - 1);
            }
        }

        swap(teachplan, TeachPlanDto);
        //批量修改
        ArrayList<Teachplan> teachplans = new ArrayList<>();
        teachplans.add(teachplan);
        teachplans.add(TeachPlanDto);
        boolean b = updateBatchById(teachplans);
        if (!b) {
            throw new XueChengPlusException("移动失败");
        }


    }

    private void swap(Teachplan teachplan, Teachplan TeachPlanDto) {
        Integer one = teachplan.getOrderby();
        Integer two = TeachPlanDto.getOrderby();
        //交换
        teachplan.setOrderby(two);
        TeachPlanDto.setOrderby(one);
    }


    private List<Teachplan> getChildTreeById(Long id,Long courseId) {
        List<Teachplan> list = this.getBaseMapper().getChildTreeById(id,courseId);
        return list;
    }


}
