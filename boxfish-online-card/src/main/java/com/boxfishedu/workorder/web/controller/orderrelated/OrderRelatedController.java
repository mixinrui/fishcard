package com.boxfishedu.workorder.web.controller.orderrelated;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.servicex.orderrelated.OrderConsumeInfoServiceX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/3/31.
 * 与订单相关的接口操作,目前主要为与订单系统相关的操作
 */
@CrossOrigin
@RestController
@RequestMapping("/service/order")
public class OrderRelatedController {
    @Autowired
    private OrderConsumeInfoServiceX orderConsumeInfoServiceX;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/{order_id}/info", method = RequestMethod.GET)
    public JsonResultModel getOrderConsumeInfo(@PathVariable("order_id") Long orderId) {
        JsonResultModel jsonResultModel = orderConsumeInfoServiceX.getOrderConsumeInfo(orderId);
        return jsonResultModel;
    }






}