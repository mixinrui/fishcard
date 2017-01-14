package com.boxfishedu.workorder.web.view.teacher;

import com.boxfishedu.workorder.web.view.base.BaseView;
import com.boxfishedu.workorder.web.view.course.CourseView;
import lombok.Data;
import java.util.Date;
import java.util.Set;

/**
 * Created by hucl on 16/3/11.
 */
@Data
public class TeacherView  extends BaseView {
    private Set<RoleView> roles;
    private Set<CourseView> courses;
    private Long teacherId;
    private String teacherName;
    private Long id;
    private String name;
    private Long boxFishId;
    private String avatar;
    private String nickname;
    private String courseScheduleId;
    //此处gender使用了枚举,是否应该修改为枚举
    private String gender;
    private Date birthday;
    private String school;
    private Long addressId;
    private String address;
    private String telephone1;
    private String telephone2;
    private String email;
    private Long qq;
    private String weiXin;
    private String gpsLongitude;
    private String gpsLatitude;
    private String baiDuLongitude;
    private String baiDuLatitude;
    private String payAli;
    private String payWeiXin;
    private String identificationType;
    private String identificationValue;
    private Integer flagActive;
    private Integer serviceCount;
    private Long plannerId;
    private Long answerId;
    private Boolean select;
}
