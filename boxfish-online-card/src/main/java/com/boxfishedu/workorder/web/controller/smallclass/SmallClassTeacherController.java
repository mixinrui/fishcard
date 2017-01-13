package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

/**
 * Created by hucl on 17/1/7.
 */
@CrossOrigin
@RestController
@RequestMapping("/service/teacher/smallclass")
public class SmallClassTeacherController {

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @RequestMapping(value = "/{smallclass_id}/validate", method = RequestMethod.GET)
    public JsonResultModel validate(@PathVariable("smallclass_id") Long smallClassId) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        //10:too early   20:completed   30:success
        map.put("status", 30);
        map.put("statusDesc", "success");
        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);

        map.put("classInfo", smallClass);
        return JsonResultModel.newJsonResultModel(map);
    }

    @RequestMapping(value = "/{smallclass_id}/detail", method = RequestMethod.GET)
    public JsonResultModel classDetail(@PathVariable("smallclass_id") Long smallClassId) {
        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        return JsonResultModel.newJsonResultModel(smallClass);
    }

    @RequestMapping(value = "/{smallclass_id}/status", method = RequestMethod.POST)
    public JsonResultModel status(@RequestBody Map<String, String> statusReport) {
        return JsonResultModel.newJsonResultModel("success");
    }
}
