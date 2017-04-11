package com.boxfishedu.workorder.web.controller.monitor;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.service.monitor.MonitorUserService;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by ansel on 2017/3/16.
 */
@RestController
@RequestMapping(value = "/class/monitor")
public class ClassMonitorController {

    @Autowired
    MonitorUserService monitorUserService;

    @Autowired
    CommonServeServiceX commonServeServiceX;

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public JsonResultModel page(String classType,Date startTime,Date endTime,Pageable pageable,Long userId){
        return JsonResultModel.newJsonResultModel(monitorUserService.page(classType,startTime,endTime,userId,pageable));
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public Object detailList(String classType,Pageable pageable, Date startTime, Date endTime,Long studentId, Long userId){
        commonServeServiceX.checkToken(studentId, userId);
        return monitorUserService.detailList(classType,startTime,endTime,userId,pageable);
    }

    @RequestMapping(value = "/super/user", method = RequestMethod.GET)
    public Object superUser(){
        return JsonResultModel.newJsonResultModel(monitorUserService.getAllSuperUser());
    }


    @RequestMapping(value = "/change/monitor/flag", method = RequestMethod.POST)
    public Object changeMonitorFlag(@RequestParam(value = "userId") Long userId,
                                  @RequestParam(value = "classId")  Long classId,
                                  @RequestParam(value = "classType")  String classType){
        monitorUserService.changeMonitorFlag(userId,classId,classType);
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/get/teacher/app/release", method = RequestMethod.GET)
    public Object getTeacherApp(@RequestParam(value = "userId") Long userId,
                                @RequestParam(value = "userId2")  Long classId){

        List list = new ArrayList();
        list.add(userId);
        list.add(classId);
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData(monitorUserService.getTeacherAppRelease(list));
        return jsonResultModel;
    }
}
