package com.boxfishedu.workorder.web.controller.clientservice.backplat;

import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.mall.enums.OrderChannelDesc;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardQueryServiceX;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电话客服后台支持
 */
@CrossOrigin
@RestController
@RequestMapping("/clientservice/backplat")
public class FishCardQueryOutController {
    @Autowired
    private FishCardQueryServiceX fishCardQueryServiceX;

    private Logger logger= LoggerFactory.getLogger(this.getClass());


    /**
     * 用户id不做限制的查询
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public JsonResultModel listFishCardsByCond(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        return fishCardQueryServiceX.listFishCardsByUnlimitedUserCond(fishCardFilterParam,pageable);
    }
}
