package com.boxfishedu.workorder.servicex.balancecenter;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/5/10.
 * 结算中心的查询接口
 */
@Component
public class FishCardBalanceQueryServiceX {
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ServeService serveService;

    public JsonResultModel listFishCardsByCond(FishCardFilterParam fishCardFilterParam) {
        //验证参数的有效性
        workOrderService.processFilterParam(fishCardFilterParam, true);
        //根据参数分条件查询
        List<WorkOrderView> workOrderViews = listFishCardsByDifferentCond(fishCardFilterParam);
        for (WorkOrderView workOrderView : workOrderViews) {
            workOrderView.setStatusDesc(FishCardStatusEnum.getDesc(workOrderView.getStatus()));
        }
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(workOrderViews);
        return jsonResultModel;
    }

    private List<WorkOrderView> listFishCardsByDifferentCond(FishCardFilterParam fishCardFilterParam) {
        return workOrderService.findByQueryCondAllStatusForTeacher(fishCardFilterParam.getTeacherId(),
                fishCardFilterParam.getBeginDateFormat(), fishCardFilterParam.getEndDateFormat(), fishCardFilterParam.getStatus());
    }
}
