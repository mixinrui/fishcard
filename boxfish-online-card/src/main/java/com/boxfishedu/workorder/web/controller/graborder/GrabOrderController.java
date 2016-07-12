package com.boxfishedu.workorder.web.controller.graborder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.servicex.graborder.GrabOrderServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.GrabOrderView;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 抢单
 */
@CrossOrigin
@RestController
@RequestMapping("/graborder")
public class GrabOrderController {


    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GrabOrderServiceX grabOrderServiceX;

    @RequestMapping(value = "/{teacher_id}/workorderlist", method = RequestMethod.GET)
    public JsonResultModel getWorkOrderListByTeacherId(@PathVariable("teacher_id") Long teacherId) {
        return grabOrderServiceX.getWorkOrderListByTeacherId(teacherId);
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
