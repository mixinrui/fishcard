package com.boxfishedu.workorder.servicex.instantclass.config;

import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/11/22.
 */
@Data
public class DayRangeBean {
    private String date;
    private List<RangeBean> range;

    @Data
    public static class RangeBean{
        private String begin;
        private String end;
    }
}
