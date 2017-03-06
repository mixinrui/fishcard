package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;

/**
 * Created by LuoLiBing on 17/2/24.
 */
public enum SelectMessageEnum {

    NORM_CLASS_BEFORE_1(Weeks.LESS_AND_EQUALS_THAN_1, ClassTypeEnum.NORMAL, "您已选择以下时间段课程，最快后天可上课"),
    SMALL_CLASS_BEFORE_1(Weeks.LESS_AND_EQUALS_THAN_1, ClassTypeEnum.SMALL, "您已选择以下时间段课程，最快后天可上课"),
    NORM_CLASS_AFTER_1(Weeks.GREATER_THAN_1, ClassTypeEnum.NORMAL, "您已选择以下时间段课程，后%@周将与第一周上课时间相同，最快后天可上课"),
    SMALL_CLASS_AFTER_1(Weeks.GREATER_THAN_1, ClassTypeEnum.SMALL, "您已选择以下时间段课程，后%@周将与第一周上课时间相同，最快2周后可上课"),
    DEFAULT(Weeks.DEFAULT, ClassTypeEnum.NORMAL, null);

    public final Weeks totalWeeks;

    public final ClassTypeEnum classType;

    public final String message;

    SelectMessageEnum(Weeks totalWeeks, ClassTypeEnum classType, String message) {
        this.totalWeeks = totalWeeks;
        this.classType = classType;
        this.message = message;
    }


    public enum Weeks {
        LESS_AND_EQUALS_THAN_1(1), GREATER_THAN_1(2), DEFAULT(0);

        public final int week;

        Weeks(int week) {
            this.week = week;
        }

        public static Weeks resolve(int week) {
            if(week <= 1) {
                return LESS_AND_EQUALS_THAN_1;
            } else {
                return GREATER_THAN_1;
            }
        }
    }

    // 本来想用多路分发的方式不容易出错, 但是看看还是算了太复杂了, 规模再大一点再使用
    public static SelectMessageEnum resolve(ClassTypeEnum classType, Weeks weeks) {
        // 小班课
        if(classType == ClassTypeEnum.SMALL) {
            if(weeks == Weeks.LESS_AND_EQUALS_THAN_1) {
                return SMALL_CLASS_BEFORE_1;
            } else {
                return SMALL_CLASS_AFTER_1;
            }
        }
        // 普通课
        else {
            if(weeks == Weeks.LESS_AND_EQUALS_THAN_1) {
                return NORM_CLASS_BEFORE_1;
            } else {
                return NORM_CLASS_AFTER_1;
            }
        }
    }
}
