package com.boxfishedu.workorder.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by hucl on 16/7/8.
 */
@Data
public class TeachingOnlineMsg {
    private Long user_id;
    private String push_title;
    private String push_type;// 推送消息类型
    private TeachingOnlineMsgAttach data;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TeachingOnlineMsgAttach{
        private String type;
        private Integer count;
        private Long studentId;
        private String day;
        private Long slotId;
        private Long cardId;
    }
}


