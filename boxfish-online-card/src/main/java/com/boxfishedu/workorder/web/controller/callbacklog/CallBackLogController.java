package com.boxfishedu.workorder.web.controller.callbacklog;

import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.servicex.callbacklog.CallBackLogServiceX;
import com.boxfishedu.workorder.web.param.callbacklog.CallBackHeartBeatParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by hucl on 17/2/16.
 */
@RestController
@RequestMapping(value = "/callback/log")
public class CallBackLogController {

    @Autowired
    private CallBackLogServiceX callBackLogServiceX;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //不只是heartBeat
    @RequestMapping(value = "/heartBeat", method = RequestMethod.POST)
    public Object heartBeat(@RequestBody CallBackHeartBeatParam param) {
        callBackLogServiceX.updateSet(param);
        return JsonResultModel.newJsonResultModel("OK");
    }

    public Object teacherOperation(){
        return JsonResultModel.newJsonResultModel("OK");
    }

//    @RequestMapping(value = "/heartBeat", method = RequestMethod.POST)
//    public Object heartBeat(@RequestBody Map<String, Object> param) {
//
//        CallBackHeartBeatParam callBackHeartBeatParam
//                = JacksonUtil.readValue(JacksonUtil.toJSon(param), CallBackHeartBeatParam.class);
//
//        callBackLogServiceX.updateSet(callBackHeartBeatParam);
//
//        return JsonResultModel.newJsonResultModel(JacksonUtil.toJSon(param));
//    }
}
