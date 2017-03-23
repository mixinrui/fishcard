package com.boxfishedu.workorder.web.controller.smallclass;

/**
 * Created by hucl on 17/3/22.
 */

import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassBackServiceX;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassQueryServiceX;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.TrialSmallClassParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo/service/backend/")
public class DemoSmallClassBackController {
    @Autowired
    private SmallClassQueryServiceX smallClassQueryServiceX;

    @Autowired
    private SmallClassBackServiceX smallClassBackServiceX;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/smallclass/list", method = RequestMethod.GET)
    public JsonResultModel demoSmalllist(PublicFilterParam publicFilterParam, Pageable pageable) {
        return smallClassQueryServiceX.listDemoSmallClass(publicFilterParam, pageable);
    }

    @RequestMapping(value = "/smallclass/{smallClassId}", method = RequestMethod.DELETE)
    public JsonResultModel deleteDemo(@PathVariable("smallClassId") Long smallClassId) {
        return JsonResultModel.EMPTY;
    }

    @RequestMapping(value = "/smallclass", method = RequestMethod.POST)
    public JsonResultModel buildTrialSmallClass(@RequestBody TrialSmallClassParam trialSmallClassParam) {
        logger.debug("@buildTrialSmallClass创建试讲小班课,参数[{}]", JacksonUtil.toJSon(trialSmallClassParam));
        smallClassBackServiceX.buildTrialSmallClass(trialSmallClassParam);
        return JsonResultModel.newJsonResultModel("OK");
    }
}
