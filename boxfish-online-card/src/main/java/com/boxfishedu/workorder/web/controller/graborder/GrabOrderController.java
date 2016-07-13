package com.boxfishedu.workorder.web.controller.graborder;

import com.boxfishedu.workorder.servicex.graborder.GrabOrderServiceX;
import com.boxfishedu.workorder.servicex.graborder.MakeWorkOrderServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.GrabOrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private MakeWorkOrderServiceX makeWorkOrderServiceX;

    @RequestMapping(value = "/{teacher_id}/workorderlist", method = RequestMethod.GET)
    public JsonResultModel getWorkOrderListByTeacherId(@PathVariable("teacher_id") Long teacherId) {
        return grabOrderServiceX.getWorkOrderListByTeacherId(teacherId);
    }

    @RequestMapping(value = "/graboneorder", method = RequestMethod.POST)
    public JsonResultModel grabOrder(@RequestBody GrabOrderView grabOrderView) {
        return grabOrderServiceX.grabOrderByOneTeacher(grabOrderView);
    }


    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public JsonResultModel test() {
        makeWorkOrderServiceX.makeSendWorkOrder();
        return null;
    }


}
