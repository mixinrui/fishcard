package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.servicex.fishcardcenter.MakeUpLessionPickerServiceX;
import com.boxfishedu.workorder.servicex.fishcardcenter.MakeUpLessionServiceX;
import com.boxfishedu.workorder.web.param.MakeUpCourseParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hucl on 16/6/17.
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/makeup")
public class FishCardMakeUpController {
    @Autowired
    private MakeUpLessionPickerServiceX makeUpLessionPickerServiceX;

    @Autowired
    private MakeUpLessionServiceX makeUpLessionServiceX;


    /**
     *获取补课的可选时间列表
     */
    @RequestMapping(value = "/{workorder_id}/slots", method = RequestMethod.GET)
    public JsonResultModel listAvaliableSlots(@PathVariable("workorder_id") Long fishcardId) {
       return makeUpLessionPickerServiceX.listAvaliableSlots(fishcardId);
    }

    /**
     *点击补课触发操作
     */
    @RequestMapping(value = "/modification", method = RequestMethod.POST)
    public JsonResultModel makeUpCourse(@RequestBody MakeUpCourseParam makeUpCourseParam){
        makeUpLessionServiceX.makeUpCourse(makeUpCourseParam);
        return JsonResultModel.newJsonResultModel(null);
    }

    /**
     * 更改鱼卡状态(jiaozijun新增)
     */
    @RequestMapping(value = "/fishcard/status/change", method = RequestMethod.POST)
    public JsonResultModel fishcardStatusChange(@RequestBody MakeUpCourseParam makeUpCourseParam){
        return makeUpLessionServiceX.fishcardStatusChange(makeUpCourseParam);
    }

    /**
     * 批量确认鱼卡  确认状态
     * @param makeUpCourseParam
     * @return
     */
    @RequestMapping(value = "/fishcard/confirmstatus/change", method = RequestMethod.POST)
    public JsonResultModel fishcardConfirmStatusChange(@RequestBody MakeUpCourseParam makeUpCourseParam){
        return makeUpLessionServiceX.fishcardStatusRechargeChange(makeUpCourseParam);
    }


    /**
     * 页面触发退款申请
     * @param makeUpCourseParam
     * @return
     */
    @RequestMapping(value = "/fishcard/confirm/recharge", method = RequestMethod.POST)
    public JsonResultModel fishcardConfirmStatusRecharge(@RequestBody MakeUpCourseParam makeUpCourseParam){
        return makeUpLessionServiceX.fishcardConfirmStatusRecharge(makeUpCourseParam);
    }


//    /**
//     * 订单系统调用http方式进行退款状态回馈
//     * @param makeUpCourseParam  ============>详见  FishCardMakeUpRechargeController
//     * @return
//     */
//    @RequestMapping(value = "/fishcard/laststate/change", method = RequestMethod.POST)
//    public JsonResultModel fixedStateFromOrder(@RequestBody MakeUpCourseParam makeUpCourseParam){
//        return makeUpLessionServiceX.fixedStateFromOrder(makeUpCourseParam);
//    }


}
