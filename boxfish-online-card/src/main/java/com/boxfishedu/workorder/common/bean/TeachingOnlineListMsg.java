package com.boxfishedu.workorder.common.bean;

import com.boxfishedu.online.order.entity.TeacherForm;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by jiaozijun on 16/7/8.
 */
@Data
public class TeachingOnlineListMsg {
    private List<TeachingOnlineMsg> teachingOnlineMsg = Lists.newArrayList();
}
