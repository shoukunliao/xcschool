package com.xuecheng.model.dto;

import com.xuecheng.model.po.Teachplan;
import com.xuecheng.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

@Data
public class TeachPlanDto extends Teachplan {

    private List<TeachPlanDto> teachPlanTreeNodes;

    private TeachplanMedia teachplanMedia;


}
