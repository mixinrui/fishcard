package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.FishCardinnerParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 供内网的外部接口 无需token验证
 * Created by jiaozijun on 16/12/3.
 */
@CrossOrigin
@RestController
@RequestMapping("/inner/fishcard")
public class FishCardQueryOuterController {

    @Autowired
    private WorkOrderService workOrderService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());
    /**
     * 获取某个学生的所有的鱼卡信息
     * @param fishCardinnerParam
     * @return
     */
    @RequestMapping(value = "/studentlist",method = RequestMethod.GET)
    public JsonResultModel getFishCardListByStudentId(FishCardinnerParam fishCardinnerParam){
        Long studentId = fishCardinnerParam.getStudentId();
        if(null==studentId || 0==studentId)
            throw new BusinessException("参数有问题");
        List<WorkOrder> myWorkOrdes = workOrderService.findByStudentId(studentId);

        myWorkOrdes.stream().filter(workOrder1 -> (1==workOrder1.getIsFreeze() || workOrder1.getStatus() <40)).collect(Collectors.toList());

        if(null!=fishCardinnerParam.getSkuId()){
            myWorkOrdes.stream().filter(workOrder1 -> (fishCardinnerParam.getSkuId().equals( workOrder1.getSkuId() ) )).collect(Collectors.toList());
        }

        if(CollectionUtils.isEmpty(myWorkOrdes)){
            return JsonResultModel.newJsonResultModel(myWorkOrdes);
        }
        if(null==fishCardinnerParam.getHasEvFishcardIds() || fishCardinnerParam.getHasEvFishcardIds().length==0){
            return JsonResultModel.newJsonResultModel(myWorkOrdes);
        }else {
            List<Long>  hasEvFinsList =  Arrays.asList(fishCardinnerParam.getHasEvFishcardIds());
            myWorkOrdes=myWorkOrdes.stream().filter(workOrder -> !hasEvFinsList.contains(workOrder.getId())).collect(Collectors.toList());
             return JsonResultModel.newJsonResultModel(myWorkOrdes);
        }

    }
}
