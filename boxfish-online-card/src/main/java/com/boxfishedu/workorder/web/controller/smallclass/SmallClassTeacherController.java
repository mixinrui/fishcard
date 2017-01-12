package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
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
    @RequestMapping(value = "/{smallclass_id}/validate", method = RequestMethod.GET)
    public JsonResultModel validate(@PathVariable("smallclass_id") Long smallClassId) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        //10:too early   20:completed   30:success
        map.put("status", 30);
        map.put("statusDesc", "success");

        SmallClass smallClass = new SmallClass();
        smallClass.setGroupLeaderCard(1L);
        smallClass.setGroupLeader(11L);
        smallClass.setClassType(ClassTypeEnum.SMALL.name());
        smallClass.setRoleId(2);
        smallClass.setChatRoomId(111111L);
        smallClass.setClassDate(new Date());
        smallClass.setCourseId("JJJKKKKKLLLSS");
        smallClass.setCourseName("test course");
        smallClass.setCourseType("TALK");
        smallClass.setCreateTime(new Date());
        smallClass.setGroupId("group id");
        smallClass.setGroupName("group name");
        smallClass.setStatus(200);
        smallClass.setSlotId(20);
        smallClass.setTeacherId(222L);
        smallClass.setUpdateTime(new Date());
        smallClass.setId(333L);

        map.put("classInfo", smallClass);
        return JsonResultModel.newJsonResultModel(map);
    }

    @RequestMapping(value = "/{smallclass_id}/detail", method = RequestMethod.GET)
    public JsonResultModel classDetail(@PathVariable("smallclass_id") Long smallClassId) {
        SmallClass smallClass = new SmallClass();
        smallClass.setGroupLeaderCard(1L);
        smallClass.setGroupLeader(11L);
        smallClass.setClassType(ClassTypeEnum.PUBLIC.name());
        smallClass.setRoleId(2);
        smallClass.setChatRoomId(111111L);
        smallClass.setClassDate(new Date());
        smallClass.setCourseId("JJJKKKKKLLLSS");
        smallClass.setCourseName("test course");
        smallClass.setCourseType("TALK");
        smallClass.setCreateTime(new Date());
        smallClass.setGroupId("group id");
        smallClass.setGroupName("group name");
        smallClass.setStatus(200);
        smallClass.setSlotId(20);
        smallClass.setTeacherId(222L);
        smallClass.setUpdateTime(new Date());
        smallClass.setId(333L);
        return JsonResultModel.newJsonResultModel(smallClass);
    }

    @RequestMapping(value = "/{smallclass_id}/status", method = RequestMethod.POST)
    public JsonResultModel status(@RequestBody Map<String, String> statusReport) {
        return JsonResultModel.newJsonResultModel("success");
    }
}
