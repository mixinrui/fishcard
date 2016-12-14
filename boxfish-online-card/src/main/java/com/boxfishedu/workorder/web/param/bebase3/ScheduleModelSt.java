package com.boxfishedu.workorder.web.param.bebase3;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by wangshichao on 16/4/12.
 */
@Data
public class ScheduleModelSt {

    private Long Id;

    @NotNull(message = "日期不能为空")
    private Date day;

    private String courseType;

    @NotNull(message = "时间片Id不能为空")
    private Integer slotId;

    private Integer roleId;

    private Integer matchStatus   ;// '申请状态  0 不匹配  1 匹配  2 无时间片待匹配

}
