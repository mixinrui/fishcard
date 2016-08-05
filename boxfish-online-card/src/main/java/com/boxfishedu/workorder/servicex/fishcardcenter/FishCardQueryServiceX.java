package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.bean.SkuTypeEnum;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardQueryService;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/5/9.
 */
@Component
public class FishCardQueryServiceX {
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private FishCardQueryService fishCardQueryService;

    @Autowired
    private ServeService serveService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public JsonResultModel listFishCardsByUnlimitedUserCond(FishCardFilterParam fishCardFilterParam,Pageable pageable){
        workOrderService.processDateParam(fishCardFilterParam);
        List<WorkOrder> workOrderList=fishCardQueryService.filterFishCards(fishCardFilterParam,pageable);
        Long count=fishCardQueryService.filterFishCardsCount(fishCardFilterParam);
        Page<WorkOrder> page = new PageImpl(workOrderList, pageable, count);
        return JsonResultModel.newJsonResultModel(page);
    }


    /**
     * Excel导出
     * @param fishCardFilterParam
     * @param pageable
     * @return
     */
    public List<WorkOrder>  listFishCardsByUnlimitedUserCondForExcel(FishCardFilterParam fishCardFilterParam,Pageable pageable){
        workOrderService.processDateParam(fishCardFilterParam);
        return fishCardQueryService.filterFishCards(fishCardFilterParam,pageable);
    }

    //带用户的访问.这个接口在规划管理的页面做出来后会使用
    public JsonResultModel listFishCardsByCond(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        workOrderService.processFilterParam(fishCardFilterParam, false);
        List<Service> services = ListServicesByCond(fishCardFilterParam);
        if (CollectionUtils.isEmpty(services)) {
            logger.error("无对应的服务");
            return JsonResultModel.newJsonResultModel(null);
        }
        Page<WorkOrder> page = listFishCardsByDifferentCond(fishCardFilterParam, services, pageable);
        for (WorkOrder workOrder : page.getContent()) {
            workOrder.setStatusDesc(FishCardStatusEnum.getDesc((workOrder.getStatus())));
            workOrder.setSkuId(workOrder.getService().getSkuId());
            if(ConstantUtil.SKU_EXTRA_VALUE==workOrder.getSkuIdExtra()){
                workOrder.setTeachingType(TeachingType.WAIJIAO.getCode());
            }
            else{
                workOrder.setTeachingType(TeachingType.ZHONGJIAO.getCode());
            }
        }
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(page);
        return jsonResultModel;
    }

    private List<Service> ListServicesByCond(FishCardFilterParam fishCardFilterParam) {
        if(null==fishCardFilterParam.getStudentId()){

        }
        if (null != fishCardFilterParam.getOrderCode()) {
            return serveService.findServicesByUserAndCond(fishCardFilterParam.getStudentId(), fishCardFilterParam.getOrderCode()
                    , SkuTypeEnum.SKU_COURSE_PLAN.value(), SkuTypeEnum.SKU_ANSWER_ONLINE.value());
        } else {
            return serveService.findAllServicesByUser(fishCardFilterParam.getStudentId(), SkuTypeEnum.SKU_COURSE_PLAN.value()
                    , SkuTypeEnum.SKU_ANSWER_ONLINE.value());
        }
    }

    private Page<WorkOrder> listFishCardsByDifferentCond(FishCardFilterParam fishCardFilterParam, List<Service> services, Pageable pageable) {
        List<Long> ids = new ArrayList<>();
        for (Service service : services) {
            ids.add(service.getId());
        }
        Long[] idsArr = new Long[ids.size()];
        if (null == fishCardFilterParam.getStatus()) {
            return workOrderService.findByQueryCondAllStatus(fishCardFilterParam, ids.toArray(idsArr), pageable);
        } else {
            return workOrderService.findByQueryCondSpecialStatus(fishCardFilterParam, ids.toArray(idsArr), pageable);
        }
    }

    public JsonResultModel listAllStatus(){
        Map<Integer,String> statusMap= Maps.newHashMap();
        for(FishCardStatusEnum fishCardStatusEnum:FishCardStatusEnum.values()){
            statusMap.put(fishCardStatusEnum.getCode(),fishCardStatusEnum.getDesc());
        }
        return JsonResultModel.newJsonResultModel(statusMap);
    }
}
