package com.boxfishedu.workorder.web.param.fishcardcenetr;

import com.boxfishedu.workorder.common.bean.ChannelTypeEnum;
import lombok.Data;

/**
 * 小班课补学生向订单发送请求
 * jiaozijun
 */
@Data
public class StudentSysParam {

    private String channel = "ONLINE";

    private ChannelTypeEnum channel_type;

    private Short finished = new Short((short) 1);
}
