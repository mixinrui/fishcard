package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by jiaozijun on 16/12/5.
 */
@Data
public class StudentNotifyParam {
    private Long [] studentIds;//学生id列表
    private  Boolean appreceiveFlag ; // 是否发送app推送
}
