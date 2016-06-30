package com.boxfishedu.workorder.web.controller;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.CommonWorkOrderServiceX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by hucl on 16/3/31.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/common")
public class CommonServiceController {
    @Autowired
    private CommonWorkOrderServiceX commonWorkOrderServiceX;
    @Autowired
    private CommonServeServiceX commonServeServiceX;

    /**
     * 根据工单id获取工单详情
     *
     * @param workorderId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{workorder_id}", method = RequestMethod.GET)
    public JsonResultModel getWorkorderById(@PathVariable("workorder_id") Long workorderId, String userId) {
        return JsonResultModel.newJsonResultModel(commonWorkOrderServiceX.findWorkOrderById(workorderId));
    }

    /**
     * 根据订单id获取剩余课程
     * param:
     * [1,2,3,4,5,6,7,8,9]
     *
     * @return
     */
    @RequestMapping(value = "/surplus", method = RequestMethod.POST)
    public JsonResultModel getAmountofSurplus(@RequestBody List<Long> ids) {
        return commonServeServiceX.getAmountofSurplus(ids);
    }
}
