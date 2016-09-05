package com.boxfishedu.card.comment.manage.entity.form;

/**
 * Created by LuoLiBing on 16/9/5.
 */
public enum CommentCardFormStatus {
    NOTANSWER(0, "未点评"), ANSWERED(1, "已回答"), TIMEOUT(2, "超时未完成"), ALL(3, "全部");

    private int code;

    private String desc;

    CommentCardFormStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int value() {
        return code;
    }
}
