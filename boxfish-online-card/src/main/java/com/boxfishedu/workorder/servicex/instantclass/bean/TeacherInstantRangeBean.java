package com.boxfishedu.workorder.servicex.instantclass.bean;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/11/23.
 */
@Data
public class TeacherInstantRangeBean {
    private String date;
    private List<InstantRange> range;

    public static TeacherInstantRangeBean getInstantRange(List<InstantClassTimeRules> instantClassTimeRules){
        TeacherInstantRangeBean teacherInstantRangeBean=new TeacherInstantRangeBean();
        List<InstantRange> instantRanges= Lists.newArrayList();
        teacherInstantRangeBean.setDate(instantClassTimeRules.get(0).getDate());
        instantClassTimeRules.forEach(item -> {
            InstantRange instantRange=new InstantRange();
            instantRange.setBegin(item.getBegin());
            instantRange.setEnd(item.getEnd());
            instantRanges.add(instantRange);
        });
        teacherInstantRangeBean.setRange(instantRanges);
        return teacherInstantRangeBean;
    }

    public static TeacherInstantRangeBean defaultRange(){
        TeacherInstantRangeBean teacherInstantRangeBean=new TeacherInstantRangeBean();
        teacherInstantRangeBean.setDate(DateUtil.date2SimpleString(new Date()));
        InstantRange instantRange=new InstantRange();
        instantRange.setBegin("00:00:00");
        instantRange.setEnd("23:59:59");
        teacherInstantRangeBean.setRange(Arrays.asList(instantRange));
        return teacherInstantRangeBean;
    }

    @Data
    static class InstantRange{
        private String begin;
        private String end;
    }


}
