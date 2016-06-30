package com.boxfishedu.workorder.servicex.bean;

/**
 * Created by LuoLiBing on 16/4/23.
 */
public enum TimeSlotsStatus {
    FREE(0), SELECTED(1), ASSIGNED(2);
    private Integer code;
    TimeSlotsStatus(Integer code) {
        this.code = code;
    }
    public Integer value() {
        return code;
    }

    @Override
    public String toString() {
        if(this.code == null) {
            return "";
        }
        return String.valueOf(this.code);
    }
}
