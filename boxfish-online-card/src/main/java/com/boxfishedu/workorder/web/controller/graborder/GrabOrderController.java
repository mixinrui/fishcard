package com.boxfishedu.workorder.web.controller.graborder;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 此接口主要提供给鱼卡中心的管理后台使用,主要包括:鱼卡列表,换课,换教师,换时间
 */
@CrossOrigin
@RestController
@RequestMapping("/graporder")
public class GrabOrderController {


    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/{teacher_id}/workorderlist", method = RequestMethod.GET)
    public JsonResultModel getWorkOrderListByTeacherId(@PathVariable("teacher_id") Long teacherId) {

        List<WorkOrderGrab> fishCardList = Lists.newArrayList();

        Long workOrderId1 = 111111111L;
        Long workOrderId2 = 222222222L;

        WorkOrderGrab workOrderGrabMock1 = new WorkOrderGrab();
        workOrderGrabMock1.setWorkorderId(workOrderId1);
        workOrderGrabMock1.setStartTime(new Date());
        fishCardList.add(workOrderGrabMock1);

        WorkOrderGrab workOrderGrabMock2 = new WorkOrderGrab();
        workOrderGrabMock2.setWorkorderId(workOrderId2);
        workOrderGrabMock2.setStartTime(new Date());
        fishCardList.add(workOrderGrabMock2);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", WorkOrderConstant.FISHCARD_LIST);
        jsonObject.put("code",0);
        jsonObject.put("workorderlist",fishCardList);

        return JsonResultModel.newJsonResultModel(jsonObject);
    }


    @RequestMapping(value = "/graborder", method = RequestMethod.POST)
    public JsonResultModel grabOrder() {
        int i = (int)(Math.random()*2);
        JSONObject jsonObject = new JSONObject();
        if(i==0){
            jsonObject.put("msg", WorkOrderConstant.GRABORDER_SUCCESS);
            jsonObject.put("code",0);
        }else{
            jsonObject.put("msg", WorkOrderConstant.GRABORDER_FAIL);
            jsonObject.put("code",1);
        }
        return JsonResultModel.newJsonResultModel(jsonObject);
    }



}
