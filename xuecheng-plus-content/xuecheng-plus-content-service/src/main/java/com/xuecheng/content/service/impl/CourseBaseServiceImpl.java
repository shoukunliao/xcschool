package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.service.ICourseBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements ICourseBaseService {

    @Override
    public PageResult<CourseBase> listByQuery(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //校验数据

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        LambdaQueryWrapper<CourseBase> lqw = new LambdaQueryWrapper<>();
        lqw.like(!StringUtils.isEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName())
                .eq(!StringUtils.isEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus())
                .eq(!StringUtils.isEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());


        page = page(page, lqw);
        List<CourseBase> records = page.getRecords();

        return new PageResult<>(records,page.getTotal(),page.getCurrent(),page.getSize());


    }
}
