package com.boxfishedu.workorder.web.controller.studentrelated;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.*;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * 更改学生上课时间 供app调用
 * @author jiaozijun
 */
@CrossOrigin
@RestController
@RequestMapping("/service/student/changeTime")
public class
StudentAppRelatedChangeClassTimeController {
    @Autowired
    private CommonServeServiceX commonServeServiceX;
    @Autowired
    private AvaliableTimeServiceXV1 avaliableTimeServiceXV1;

    @Autowired
    private AvaliableTimeForChangeTimeServiceXV avaliableTimeForChangeTimeServiceXV;


    /**
     * 获取更改鱼卡时间片 信息
     * @param workOrderId
     * @param userId
     * @return
     * @throws CloneNotSupportedException
     */
    @RequestMapping(value = "/v2/time/available", method = RequestMethod.GET)
    public JsonResultModel timeAvailableV1(Long workOrderId, Long userId) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(userId,userId);
        return avaliableTimeForChangeTimeServiceXV.getTimeAvailable(workOrderId);
    }


}
