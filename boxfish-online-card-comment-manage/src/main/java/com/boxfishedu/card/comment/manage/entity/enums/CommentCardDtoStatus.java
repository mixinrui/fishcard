package com.boxfishedu.card.comment.manage.entity.enums;

/**
 * Created by LuoLiBing on 16/9/8.
 */
public enum CommentCardDtoStatus {

    UNKNOW(-1,"未知"), NOTANSWER0(0,"未点评"),NOTANSWER1(1, "未点评"), NOTANSWER2(2, "未点评"), ANSWERED(3, "已点评"), TIMEOUT(4, "超时未点评"), EVALUATED(5, "学生已评价");

    private int value;

    private String desc;

    CommentCardDtoStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int value() {
        return value;
    }

    public String desc() {
        return desc;
    }
}
