package com.boxfishedu.workorder.web.controller.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.service.absenteeism.AbsenteeismService;
import com.boxfishedu.workorder.service.absenteeism.sdk.AbsenteeismSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ansel on 16/9/19.
 */
@RestController
@RequestMapping(value = "/handle/deduct/score")
public class AbsenteesimController {

    @Autowired
    AbsenteeismSDK absenteeismSDK;

    @Autowired
    AbsenteeismService absenteeismService;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Object testDeductScore(){
        return JsonResultModel.newJsonResultModel(absenteeismService.testQueryAbsentStudent());
    }

    @RequestMapping(value = "/product", method = RequestMethod.GET)
    public Object productDeductScore(){
        return JsonResultModel.newJsonResultModel(absenteeismService.productQueryAbsentStudent());
    }
}
