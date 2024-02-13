package com.xuecheng.content.service.impl;

import com.xuecheng.model.dto.CourseCategoryTreeDto;
import com.xuecheng.model.po.CourseCategory;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.service.ICourseCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-02-09
 */
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements ICourseCategoryService {




    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String root) {
        List<CourseCategoryTreeDto> selectTreeNodes =  this.baseMapper.selectTreeNodes(root);
        ArrayList<CourseCategoryTreeDto> list = new ArrayList<>();
        if (selectTreeNodes==null) {
            return list;
        }
        //将转换为map<parentid,node> (去根)
        Map<String, CourseCategoryTreeDto> map = selectTreeNodes.stream().filter(node ->
                !node.getId().equals(root)
        ).collect(Collectors.toMap(CourseCategory::getId, value -> value,(key1,key2)->key2));


        selectTreeNodes.stream().filter((node)->!node.getId().equals(root)).forEach((node)->{
            if (node.getParentid().equals(root)){
                list.add(node);
            }else {
                CourseCategoryTreeDto parentNode = map.get(node.getParentid());
                List<CourseCategoryTreeDto> childrenTreeNodes = parentNode.getChildrenTreeNodes();
                if (childrenTreeNodes==null){
                    parentNode.setChildrenTreeNodes(new ArrayList<>());
                }
                parentNode.getChildrenTreeNodes().add(node);
            }



        });
        return list;
    }
}
