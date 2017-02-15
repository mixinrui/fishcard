package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.common.util.ReflectUtil;
import com.boxfishedu.workorder.dao.mongo.ConfigBeanMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ConfigBean;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassTimerServiceX;
import com.boxfishedu.workorder.servicex.smallclass.groupbuilder.GroupBuilder;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
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

    @RequestMapping(value = "/init/config", method = RequestMethod.POST)
    public JsonResultModel addPublicWarnings(@RequestBody ConfigBean configBean) throws Exception {
        ConfigBean configDbBean = configBeanMorphiaRepository.getSingleBean();
        if (Objects.isNull(configDbBean)) {
            configDbBean = new ConfigBean();
        }
        Field[] field = configBean.getClass().getDeclaredFields();
        // 遍历所有属性
        for (int j = 0; j < field.length; j++) {
            String propertyValue = ReflectUtil.getValue(configBean, field[j].getName());
            if (!StringUtils.isEmpty(propertyValue)) {
                ReflectUtil.setValue(configDbBean, field[j].getName(), propertyValue);
            }
        }

        return JsonResultModel.newJsonResultModel("OK");
    }
}
