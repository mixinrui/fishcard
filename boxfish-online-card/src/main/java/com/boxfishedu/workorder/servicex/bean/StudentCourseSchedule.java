package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;

/**
 * Created by LuoLiBing on 16/4/22.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentCourseSchedule implements Serializable {

    private Long id;

    private String time;

    @JsonProperty(value = "teacherInfo")
    private TeacherView teacherView;

    @JsonIgnore
    private String courseId;

    @JsonProperty(value = "courseInfo")
    private CourseView courseView;

    private String courseType;

    private Long workOrderId;

    //是否冻结标记,冻结为1,其他为不冻结
    private Integer isFreeze;

    /** 由于假期原因,提示该鱼卡需要更换时间   10 需要更换时间  **/
    private Integer needChangeTime;

    private Integer status;

    private String classType;

    public String getCourseType() {
        if(courseView == null || CollectionUtils.isEmpty(courseView.getCourseType())) {
            return courseType;
        } else {
            return courseView.getCourseType().get(0);
        }
    }
}
