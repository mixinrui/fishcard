package com.boxfishedu.workorder.web.param;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Created by wangshichao on 16/4/12.
 */
@Data
public class ScheduleBatchReqSt {

    @NotNull(message = "用户id不能为空")
    private Long userId;

    private Long assginTeacherId;

    @Size(min = 1,message = "课程规划数据不能为空")
    private List<com.boxfishedu.workorder.web.param.bebase3.ScheduleModelSt> scheduleModelList;

}
