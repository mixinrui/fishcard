package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.util.Date;

/**
 * Created by hucl on 16/5/9.
 */
@Data
public class FishCardFilterParam {
    private Long studentId;
    private Long teacherId;
    private String orderCode;
    private String beginDate;
    private String endDate;
    private Date beginDateFormat;
    private Date endDateFormat;
    private Integer status;
}
