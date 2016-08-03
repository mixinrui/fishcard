package com.boxfishedu.workorder.web.controller.graborder;

import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.online.order.entity.TeacherForm;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.graborder.MakeWorkOrderService;
import com.boxfishedu.workorder.servicex.graborder.GrabOrderServiceX;
import com.boxfishedu.workorder.servicex.graborder.MakeWorkOrderServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.GrabOrderView;
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

    @Autowired
    private MakeWorkOrderServiceX makeWorkOrderServiceX;

    @Autowired
    private MakeWorkOrderService makeWorkOrderService;

    @RequestMapping(value = "/{teacher_id}/workorderlist", method = RequestMethod.GET)
    public JsonResultModel getWorkOrderListByTeacherId(@PathVariable("teacher_id") Long teacherId) {
        return grabOrderServiceX.getWorkOrderListByTeacherId(teacherId);
    }

    @RequestMapping(value = "/graboneorder", method = RequestMethod.POST)
    public JsonResultModel grabOrder(@RequestBody GrabOrderView grabOrderView) {
        logger.info("::::::::::TeacherOnLine Post params::::::::::teacherId="+grabOrderView.getTeacherId()+"&&&&workOrderId="+grabOrderView.getWorkOrderId()+"::::");
        return grabOrderServiceX.grabOrderByOneTeacher(grabOrderView);
    }

    @RequestMapping(value = "/graboneordertest", method = RequestMethod.GET)
    public JsonResultModel grabOrderTest() {
        GrabOrderView grabOrderView = new GrabOrderView();
        grabOrderView.setTeacherId(1299167L);
        grabOrderView.setWorkOrderId(8239L);
        return grabOrderServiceX.grabOrderByOneTeacher(grabOrderView);
    }


    /**
     * 提供手动触发抢课接口
     * @return
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public JsonResultModel test() {
        makeWorkOrderServiceX.makeSendWorkOrder(null, CourseTypeEnum.FUNCTION.toString());
        makeWorkOrderServiceX.makeSendWorkOrder(null, CourseTypeEnum.TALK.toString());
        //makeWorkOrderServiceX.getTeacherList("true/false");
        return new JsonResultModel();
    }

    /**
     * 提供假数据提供接口(供app端测试用,正式不用)
     * @return
     */
    @RequestMapping(value = "/testreal", method = RequestMethod.GET)
    public JsonResultModel testreal() {
        makeWorkOrderServiceX.makeSendWorkOrder("testreal",CourseTypeEnum.FUNCTION.toString());
        makeWorkOrderServiceX.makeSendWorkOrder("testreal",CourseTypeEnum.TALK.toString());
        //makeWorkOrderServiceX.getTeacherList("true/false");

        //makeWorkOrderServiceX.clearGrabData();
        return new JsonResultModel();
    }

    @RequestMapping(value = "/clearGrabData", method = RequestMethod.GET)
    public JsonResultModel clearGrabData() {
        makeWorkOrderServiceX.clearGrabData();
        return new JsonResultModel();
    }

    @RequestMapping(value = "/zhaojian", method = RequestMethod.POST)
    public JsonResultModel zhaojian(@RequestBody TeacherForm teacherForm) {
        makeWorkOrderServiceX.makeTest(teacherForm.getTeacherId());
        return new JsonResultModel();
    }




    @RequestMapping(value = "/testnew", method = RequestMethod.GET)
    public JsonResultModel testnew() {
       List<WorkOrder> list =  makeWorkOrderService.findByTeacherIdGreaterThanAndStatusAndUpdateTimeChangeCourseBetween();
        return new JsonResultModel();
    }

}
