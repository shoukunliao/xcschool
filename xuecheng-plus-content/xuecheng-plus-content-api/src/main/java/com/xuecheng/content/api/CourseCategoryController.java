package com.xuecheng.content.api;

import com.xuecheng.content.service.ICourseCategoryService;
import com.xuecheng.model.dto.CourseCategoryTreeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/course-category")
public class CourseCategoryController {

    @Autowired
    ICourseCategoryService courseCategoryService;

    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes(@RequestParam(defaultValue = "1") String root){
      return  courseCategoryService.queryTreeNodes(root);
    }
}
