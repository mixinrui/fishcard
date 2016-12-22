package com.boxfishedu.workorder.requester.param;

import lombok.Data;

/**
 * Created by jiaozijun on 16/12/22.
 */
@Data
public class TeacherParam {

    private Long teacherId;

    private String name;


    private Integer teachingType;//1 中教  2 外教  3 试讲


    //以下为外教信息
    private String firstName;

    private String lastName;

}
