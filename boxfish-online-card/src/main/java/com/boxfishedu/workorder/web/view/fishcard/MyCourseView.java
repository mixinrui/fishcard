package com.boxfishedu.workorder.web.view.fishcard;

import lombok.Data;

/**
 * Created by jiaozijun on 16/9/29.
 */
@Data
public class MyCourseView {

    public MyCourseView(String classDate,Long mount){
        this.classDate = classDate;
        this.mount = mount;
    }
    private String classDate;
    private Long mount;
}
