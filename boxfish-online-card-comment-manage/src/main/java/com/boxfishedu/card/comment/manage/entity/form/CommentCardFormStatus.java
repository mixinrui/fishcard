package com.boxfishedu.card.comment.manage.entity.form;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by LuoLiBing on 16/9/5.
 */
public enum CommentCardFormStatus {
    NOTANSWER(0, "未点评"), ANSWERED(1, "已回答"), TIMEOUT(2, "超时未完成");

    private int code;

    private String desc;

    CommentCardFormStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int value() {
        return code;
    }


    public static Map<String, Integer> getMappings() {
        Map<String, Integer> result = Maps.newLinkedHashMap();
        for(CommentCardFormStatus commentCardFormStatus : values())  {
            result.put(commentCardFormStatus.desc, commentCardFormStatus.code);
        }
        return result;
    }
}
