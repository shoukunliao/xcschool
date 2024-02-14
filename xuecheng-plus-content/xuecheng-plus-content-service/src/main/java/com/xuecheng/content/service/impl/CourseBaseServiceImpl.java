package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.service.ICourseBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.model.dto.AddCourseDto;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.EditCourseDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import com.xuecheng.model.po.CourseCategory;
import com.xuecheng.model.po.CourseMarket;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
        lqw.like(!StringUtils.isBlank(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName())
                .eq(!StringUtils.isBlank(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus())
                .eq(!StringUtils.isBlank(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());


        page = page(page, lqw);
        List<CourseBase> records = page.getRecords();

        return new PageResult<>(records,page.getTotal(),page.getCurrent(),page.getSize());


    }

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Transactional
    @Override
    public CourseBase add(Long companyId, AddCourseDto dto) {
//        //数据校验
//        if (StringUtils.isBlank(dto.getName())) {
//            throw new XueChengPlusException("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            throw new XueChengPlusException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            throw new XueChengPlusException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            throw new XueChengPlusException("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            throw new XueChengPlusException("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            throw new XueChengPlusException("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//            throw new XueChengPlusException("收费规则为空");
//        }
        //向表coursebase中插入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        courseBase.setCreateDate(LocalDateTime.now());
        int insert = courseBaseMapper.insert(courseBase);
        if (insert<=0){
            throw new XueChengPlusException("新增失败");
        }
        Long id = courseBase.getId();
        //向课程营销表保存信息
        int insert2 = insertCourseMarket(dto, id);
        if (insert2<=0){
            throw new XueChengPlusException("新增失败");
        }
        return courseBase;
    }
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Override
    public CourseBaseInfoDto getCourseBaseById(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase==null){
            throw new XueChengPlusException("id为"+courseId+"的课程不存在");
        }
        String st = courseBase.getSt();
        String sn = getCategoryNameById(st);
        String mt = courseBase.getMt();
        String mn = getCategoryNameById(mt);
        //查询详细信息(info)
        CourseMarket info = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(info,courseBaseInfoDto);
        courseBaseInfoDto.setMtName(mn);
        courseBaseInfoDto.setStName(sn);
        return courseBaseInfoDto;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBaseById(Long companyId, EditCourseDto editCourseDto) {
        //根据id查询course
        CourseBase courseBase = getById(editCourseDto.getId());
        if (courseBase==null){
            throw new XueChengPlusException("该课程不存在");
        }
        //本机构只能修改本机构的课程信息
        if (!Objects.equals(courseBase.getCompanyId(), companyId)){
            throw new XueChengPlusException("本机构只能修改本机构的课程信息");
        }
        //编辑信息
        BeanUtils.copyProperties(editCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        boolean b = updateById(courseBase);
        if (!b){
            throw new XueChengPlusException("更新失败 ");
        }
        //更新info表
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        int i = courseMarketMapper.updateById(courseMarket);
        if (i<=0){
            throw new XueChengPlusException("更新失败 ");
        }
        return getCourseBaseById(courseBase.getId());
    }

    private String getCategoryNameById(String st) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", st);
        List<CourseCategory> courseCategories = courseCategoryMapper.selectByMap(map);
        return courseCategories.get(0).getName();
    }

    private int insertCourseMarket(AddCourseDto dto, Long id) {
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        String charge = courseMarket.getCharge();
        courseMarket.setId(id);
        if (StringUtils.isBlank(charge)){
            throw new XueChengPlusException("收费规则没有选择");
        }
        //收费规则为收费
        if (charge.equals("201001")) {
            if (courseMarket.getPrice()==null || courseMarket.getPrice() <0){
                throw new XueChengPlusException("课程价格不能为空或者低于0");
            }
        }

        CourseMarket courseMarketOld = courseMarketMapper.selectById(id);
        if (courseMarketOld==null){
            return courseMarketMapper.insert(courseMarket);
        }else {
            BeanUtils.copyProperties(courseMarket,courseMarketOld);
            return courseMarketMapper.updateById(courseMarketOld);
        }
    }
}
