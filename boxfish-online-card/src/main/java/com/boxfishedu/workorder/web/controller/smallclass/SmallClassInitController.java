package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.servicex.smallclass.SmallClassTimerServiceX;
import com.boxfishedu.workorder.servicex.smallclass.groupbuilder.GroupBuilder;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hucl on 17/1/11.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/backend")
public class SmallClassInitController {
    @Autowired
    private GroupBuilder groupBuilder;

    @Autowired
    private SmallClassTimerServiceX smallClassTimerServiceX;

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public JsonResultModel buildGroup() {
        groupBuilder.group();
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/init/relation", method = RequestMethod.POST)
    public JsonResultModel buildRelation() {
        smallClassTimerServiceX.buildSmallCLassRelations();
        return JsonResultModel.newJsonResultModel("OK");
    }
}
