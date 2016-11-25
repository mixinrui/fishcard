package com.boxfishedu.workorder.common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/7/8.
 */
@Data
public class TeachingOnlineGroupMsg {
    private List<String> alias;
    private String push_title;
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


