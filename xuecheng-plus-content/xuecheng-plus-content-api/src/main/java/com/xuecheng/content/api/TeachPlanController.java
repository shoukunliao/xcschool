package com.xuecheng.content.api;

import com.xuecheng.content.service.ITeachplanService;
import com.xuecheng.model.dto.TeachPlanDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachplan")
public class TeachPlanController {


    @Autowired
    ITeachplanService iTeachplanService;

    /**
     * 获取课程计划树
     *
     * @param courseId 课程id
     * @return List<TeachPlanDto>
     */
    //GET /teachplan/22/tree-nodes
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTeachPlanTreeByCourseId(@PathVariable Long courseId) {
        return iTeachplanService.getTeachPlanTreeByCourseId(courseId);
    }
    //POST /teachplan
    //Content-Type: application/json

    @PostMapping
    public TeachPlanDto saveOrUpdate(@RequestBody TeachPlanDto teachPlanDto) {
        return iTeachplanService.insertOrUpdate(teachPlanDto);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        iTeachplanService.deleteById(id);
    }


}
