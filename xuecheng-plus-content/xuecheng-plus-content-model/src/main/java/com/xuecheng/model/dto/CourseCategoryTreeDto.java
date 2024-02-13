package com.xuecheng.model.dto;


import com.xuecheng.model.po.CourseCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    private List<CourseCategoryTreeDto> childrenTreeNodes;
}
