package com.boxfishedu.workorder.common.bean;

import lombok.Data;

/**
 * Created by hucl on 16/7/8.
 */
@Data
public class TeachingOnlineMsg {
    private Long user_id;
    private String push_title;
<<<<<<< HEAD
    private String push_type;// 推送消息类型
=======
    private TeachingOnlineMsgAttach data;

    @Data
    public static class TeachingOnlineMsgAttach{
        private String type;
        private Integer count;
    }
>>>>>>> develop
}


