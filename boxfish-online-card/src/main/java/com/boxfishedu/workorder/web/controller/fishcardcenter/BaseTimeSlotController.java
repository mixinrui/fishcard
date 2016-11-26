package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.mall.enums.OrderChannelDesc;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.service.baseTime.BaseTimeSlotService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardQueryServiceX;
import com.boxfishedu.workorder.web.param.BaseTimeSlotParam;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 此接口后台时间片操作
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/fishcard/baseslot")
public class BaseTimeSlotController {


    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BaseTimeSlotService baseTimeSlotService;


    /**
     * 查询获取基础时间片
     * @param baseTimeSlotParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/slotspage", method = RequestMethod.GET)
    public JsonResultModel listBaseSlots(BaseTimeSlotParam baseTimeSlotParam, Pageable pageable) {
        return JsonResultModel.newJsonResultModel(baseTimeSlotService.findByTeachingTypeAndClassDateBetween( baseTimeSlotParam,pageable));
    }

    @RequestMapping(value = "/slots", method = RequestMethod.GET)
    public JsonResultModel listBaseSlots(BaseTimeSlotParam baseTimeSlotParam) {
        return JsonResultModel.newJsonResultModel(baseTimeSlotService.findByTeachingTypeAndClassDateBetween( baseTimeSlotParam));
    }


}
