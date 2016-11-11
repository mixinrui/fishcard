package com.boxfishedu.workorder.common.bean.instanclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * Created by hucl on 16/9/24.
 */
@Data
public enum ClassTypeEnum {
    INSTNAT("INSTNAT"),
    GRAB("GRAB"),
    NORMAL("NORMAL");


    private String value;

    ClassTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}


