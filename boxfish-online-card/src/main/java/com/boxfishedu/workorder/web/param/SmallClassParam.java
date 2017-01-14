package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.util.Date;

/**
 *
 * Created by jiaozijun on 17/1/12.
 */

@Data
public class SmallClassParam {

    private Long id;//smallClassId
    private Long teacherId;
    private Long studentId;
    private Long smallClassId;
    private int limit;
    private int offSet;

    private String createTimeSort; //时间排序
}
