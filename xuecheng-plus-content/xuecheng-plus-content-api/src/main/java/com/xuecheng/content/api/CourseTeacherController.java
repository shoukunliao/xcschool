package com.xuecheng.content.api;

import com.xuecheng.content.service.ICourseTeacherService;
import com.xuecheng.model.po.CourseTeacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courseTeacher")
public class CourseTeacherController {
    /**
     * get /courseTeacher/list/75
     * 75为课程id，请求参数为课程id
     * <p>
     * 响应结果
     * [{"id":23,"courseId":75,"teacherName":"张老师","position":"讲师","introduction":"张老师教师简介张老师教师简介张老师教师简介张老师教师简介","photograph":null,"createDate":null}]
     */

    @Autowired
    ICourseTeacherService courseTeacherService;
    @GetMapping("/list/{courseId}")
    public List<CourseTeacher> list(@PathVariable Long courseId) {
        return courseTeacherService.listById(courseId);
    }

    //post  /courseTeacher
    //
    //请求参数：
    //{
    //  "courseId": 75,
    //  "teacherName": "王老师",
    //  "position": "教师职位",
    //  "introduction": "教师简介"
    //}
    //响应结果：
    //{"id":24,"courseId":75,"teacherName":"王老师","position":"教师职位","introduction":"教师简介","photograph":null,"createDate":null}
    /**
     * 添加课程老师
     * @param courseTeacher
     */
    @PostMapping
    public CourseTeacher save(@RequestBody CourseTeacher courseTeacher) {
        courseTeacherService.insertOrUpdate(courseTeacher);
        return courseTeacher;
    }

    //put /courseTeacher
    //请求参数：
    //{
    //  "id": 24,
    //  "courseId": 75,
    //  "teacherName": "王老师",
    //  "position": "教师职位",
    //  "introduction": "教师简介",
    //  "photograph": null,
    //  "createDate": null
    //}
    //响应：
    //{"id":24,"courseId":75,"teacherName":"王老师","position":"教师职位","introduction":"教师简介","photograph":null,"createDate":null}


//    delete /ourseTeacher/course/75/26
//
//            75:课程id
//26:教师id，即course_teacher表的主键
//    请求参数：课程id、教师id
//
//    响应：状态码200，不返回信息
@DeleteMapping("/course/{courseId}/{teacherId}")
public void delete(@PathVariable Long courseId, @PathVariable Long teacherId) {
    courseTeacherService.deleteBycIdAndtId(courseId,teacherId);

}




}
