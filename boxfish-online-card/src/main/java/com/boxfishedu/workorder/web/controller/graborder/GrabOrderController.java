package com.boxfishedu.workorder.web.controller.graborder;

import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.online.order.entity.TeacherForm;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.graborder.GrabOrderService;
import com.boxfishedu.workorder.service.graborder.MakeWorkOrderService;
import com.boxfishedu.workorder.servicex.coursenotify.CourseNotifyOneDayServiceX;
import com.boxfishedu.workorder.servicex.fishcardcenter.MakeUpLessionServiceX;
import com.boxfishedu.workorder.servicex.graborder.CourseChangeServiceX;
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

    @Autowired
    private CourseChangeServiceX courseChangeServiceX;

    @Autowired
    private CourseNotifyOneDayServiceX courseNotifyOneDayServiceX;


    @Autowired
    private  WorkOrderService  workOrderService;

    @Autowired
    private MakeUpLessionServiceX makeUpLessionServiceX;

    @Autowired
    private GrabOrderService  grabOrderService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;



    @RequestMapping(value = "/tomonotify", method = RequestMethod.GET)
    public JsonResultModel tomonotify()throws Exception {
        //测试明天有课推送
        courseNotifyOneDayServiceX.notiFyStudentClass();

        //测试今天有课
        courseNotifyOneDayServiceX.notiFyTeacherClass();
//
//        Thread.sleep(2000);
//
//        // 退款发送消息测试 推送
//        Long[]  woid = {14050L,14269L};
//        List<WorkOrder>  workOrders = workOrderService.getAllWorkOrdersByIds(woid);
//        for(WorkOrder wo:workOrders){
//            makeUpLessionServiceX.sendMessageRefund(wo);
//        }
        return new JsonResultModel();
    }
    /**
     * 根据老师获取抢单列表
     * @param teacherId
     * @return
     */
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
//        GrabOrderView grabOrderView = new GrabOrderView();
//        grabOrderView.setTeacherId(1299220L);
//        grabOrderView.setWorkOrderId(13836L);
//
//        grabOrderView.setTeacherName("冯磊");
//        grabOrderView.setState(30);
//        // grabOrderServiceX.grabOrderByOneTeacher(grabOrderView);
//
//        grabOrderService.updateTestGrab(grabOrderView);
        return JsonResultModel.newJsonResultModel();
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

    /**
     * 方便测试测试  每天18:00 向教师发送 从现在开始  未来48+6小时内 变更课程的数量  的消息
     * @return
     */
    @RequestMapping(value = "/testforcoursechange", method = RequestMethod.GET)
    public JsonResultModel testforcoursechange() {
        courseChangeServiceX.sendCourseChangeWorkOrders();
        return new JsonResultModel();
    }




}
