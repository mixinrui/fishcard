package com.boxfishedu.workorder.web.controller.fishcardcenter;

import com.boxfishedu.workorder.servicex.fishcardcenter.MakeUpLessionPickerServiceX;
import com.boxfishedu.workorder.servicex.fishcardcenter.MakeUpLessionServiceX;
import com.boxfishedu.workorder.web.param.MakeUpCourseParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单调用 不用携带token  内部接口调用
 * Created by jiaozijun on 16/6/17.
 */
@CrossOrigin
@RestController
@RequestMapping("/backend/recharge")
public class FishCardMakeUpRechargeController {

    @Autowired
    private MakeUpLessionServiceX makeUpLessionServiceX;





    /**
     * 订单系统调用http方式进行退款状态回馈
     * @param makeUpCourseParam
     * @return
     */
    @RequestMapping(value = "/fishcard/laststate/change", method = RequestMethod.POST)
    public JsonResultModel fixedStateFromOrder(@RequestBody MakeUpCourseParam makeUpCourseParam){
        return makeUpLessionServiceX.fixedStateFromOrder(makeUpCourseParam);
    }


}
