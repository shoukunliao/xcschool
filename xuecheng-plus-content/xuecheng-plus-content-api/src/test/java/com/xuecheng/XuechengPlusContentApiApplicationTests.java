package com.xuecheng;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.service.ICourseCategoryService;
import com.xuecheng.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class XuechengPlusContentApiApplicationTests {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    ICourseCategoryService courseCategoryService;
    @Test
    public void test1(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        courseCategoryTreeDtos.forEach(System.out::println);

    }
    @Test
    public void test2(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        courseCategoryTreeDtos.forEach(System.out::println);

    }

}