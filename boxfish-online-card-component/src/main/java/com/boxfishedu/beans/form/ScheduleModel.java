package com.boxfishedu.beans.form;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by wangshichao on 16/4/12.
 */
@Data
public class ScheduleModel {

    private Long Id;

    @NotNull(message = "日期不能为空")
    private Date day;

    private String courseType;

    @NotNull(message = "时间片Id不能为空")
    private Integer slotId;

    private Integer roleId;
}
