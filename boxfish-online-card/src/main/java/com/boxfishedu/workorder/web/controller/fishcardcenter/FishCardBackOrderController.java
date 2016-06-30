package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.servicex.orderrelated.BackOrderServiceX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 此接口主要提供给鱼卡中心的管理后台使用,主要包括:退课
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/backorder")
public class FishCardBackOrderController {

    @Autowired
    private BackOrderServiceX backOrderServiceX;

    /**
     * 检查订单是否满足 退单的条件
     * @param orderId
     * @return
     */
    @RequestMapping(value = "{order_id}/backorder/ordercheck" ,method = RequestMethod.GET)
    public JsonResultModel checkBackOrder(@PathVariable("order_id") Long orderId){
        JsonResultModel jsonResultModel = backOrderServiceX.judgeCanBackOrderInfo(orderId);
        return jsonResultModel;
    }


    /**
     * 退单操作
     * @param orderId
     * @return
     */
    @RequestMapping(value = "{order_id}/backorder/orderback" ,method = RequestMethod.GET)
    public JsonResultModel backOrder(@PathVariable("order_id") Long orderId){
        JsonResultModel jsonResultModel = null;
        try {
            jsonResultModel = backOrderServiceX.backForOrderByOrderId(orderId);
        }catch (Exception e){
            jsonResultModel = new JsonResultModel();
            jsonResultModel.setReturnCode(500)  ;
            jsonResultModel.setReturnMsg("系统异常");
        }
        return jsonResultModel;
    }


    /**
     * 获取退单详细
     * @param orderId
     * @return
     */
    @RequestMapping(value = "{order_id}/backorder/orderdetail",method = RequestMethod.GET)
    public JsonResultModel backOrderDetail(@PathVariable("order_id") Long orderId){
        JsonResultModel jsonResultModel = backOrderServiceX.getBackOrderDetail(orderId);
        return jsonResultModel;
    }

}
