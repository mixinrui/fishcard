package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.dao.mongo.ConfigBeanMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ConfigBean;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassTimerServiceX;
import com.boxfishedu.workorder.servicex.smallclass.groupbuilder.GroupBuilder;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

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
    private ConfigBeanMorphiaRepository configBeanMorphiaRepository;

    @Autowired
    private SmallClassTimerServiceX smallClassTimerServiceX;

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public JsonResultModel buildGroup() {
        groupBuilder.group();
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/init/{days}", method = RequestMethod.POST)
    public JsonResultModel buildGroup(Integer days) {
        groupBuilder.group(days);
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/init/relation", method = RequestMethod.POST)
    public JsonResultModel buildRelation() {
        smallClassTimerServiceX.buildSmallCLassRelations();
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/init/publicWarnings", method = RequestMethod.POST)
    public JsonResultModel addPublicWarnings(@RequestBody Map<String, String> map) {
        ConfigBean configBean = configBeanMorphiaRepository.getSingleBean();
        if (Objects.isNull(configBean)) {
            configBean = new ConfigBean();
        }
        configBean.setPublicWarning(map.get("publicWarnings"));
        configBeanMorphiaRepository.save(configBean);
        return JsonResultModel.newJsonResultModel("ok");
    }
}
