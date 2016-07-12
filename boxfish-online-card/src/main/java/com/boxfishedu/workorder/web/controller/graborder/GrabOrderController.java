package com.boxfishedu.workorder.web.controller.graborder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.GrabOrderView;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 抢单
 */
@CrossOrigin
@RestController
@RequestMapping("/graporder")
public class GrabOrderController {


    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/{teacher_id}/workorderlist", method = RequestMethod.GET)
    public JsonResultModel getWorkOrderListByTeacherId(@PathVariable("teacher_id") Long teacherId) {

        List<WorkOrderGrab> fishCardList = Lists.newArrayList();
        JSONArray jsonArray = new JSONArray();

        Long workOrderId1 = 111111111L;
        Long workOrderId2 = 222222222L;

        WorkOrderGrab workOrderGrabMock1 = new WorkOrderGrab();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("workorderId",workOrderId1);
        jsonObject1.put("startTime","2016-06-13 21:00:00");
        jsonArray.add(jsonObject1);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("workorderId",workOrderId2);
        jsonObject2.put("startTime","2016-06-13 21:30:00");
        jsonArray.add(jsonObject2);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", WorkOrderConstant.FISHCARD_LIST);
        jsonObject.put("code",0);
        jsonObject.put("workorderlist",jsonArray);

        return JsonResultModel.newJsonResultModel(jsonObject);
    }


    @RequestMapping(value = "/graboneorder", method = RequestMethod.POST)
    public JsonResultModel grabOrder(@RequestBody GrabOrderView grabOrderView) {
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
