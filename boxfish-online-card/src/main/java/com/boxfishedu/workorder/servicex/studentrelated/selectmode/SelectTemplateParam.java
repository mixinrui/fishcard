package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

import lombok.Data;

/**
 * Created by LuoLiBing on 16/9/26.
 */
@Data
public class SelectTemplateParam {

    private int loopOfWeek;

    private int numPerWeek;

    private int count;

    public SelectTemplateParam(int loopOfWeek, int numPerWeek, int count) {
        this.loopOfWeek = loopOfWeek;
        this.numPerWeek = numPerWeek;
        this.count = count;
    }
}
