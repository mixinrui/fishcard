package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/6/8.
 */
public enum FishCardDelayMsgType {
    TEACHER_ABSENT(1),STUDENT_ABSENT(2),FORCE_COMPLETE_SERVER(3),NOTIFY_TEACHER_PREPARE_CLASS(4);

    private int code;

    private FishCardDelayMsgType(int code) {
        this.code = code;
    }

    public int value() {
        return this.code;
    }

    @Override
    public String toString() {
        return String.valueOf(this.code);
    }
}
