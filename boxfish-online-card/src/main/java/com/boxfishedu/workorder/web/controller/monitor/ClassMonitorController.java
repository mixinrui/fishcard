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

    @RequestMapping(value = "/add/super/user",method = RequestMethod.POST)
    public Object addSuperUser(@RequestBody MonitorUserRequestForm monitorUserRequestForm){
        if (Objects.isNull(monitorUserRequestForm.getUserId())){
            JsonResultModel jsonResultModel = new JsonResultModel();
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("添加失败,userId不能为空!");
            jsonResultModel.setReturnCode(403);
            return jsonResultModel;
        }else {
            return JsonResultModel.newJsonResultModel(monitorUserService.addMonitorUser(monitorUserRequestForm));
        }
    }

    @RequestMapping(value = "/update/super/user",method = RequestMethod.POST)
    public Object updateSuperUser(@RequestBody MonitorUserRequestForm monitorUserRequestForm){
        if (Objects.isNull(monitorUserRequestForm.getUserId())){
            JsonResultModel jsonResultModel = new JsonResultModel();
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("修改失败,userId不能为空!");
            jsonResultModel.setReturnCode(403);
            return jsonResultModel;
        }else {
            return JsonResultModel.newJsonResultModel(monitorUserService.updateUserInfo(monitorUserRequestForm));
        }

    }

    @RequestMapping(value = "/enabled/super/user",method = RequestMethod.POST)
    public Object enabledSuperUser(@RequestParam  Long userId){
        if (Objects.isNull(userId)){
            JsonResultModel jsonResultModel = new JsonResultModel();
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("激活失败,userId不能为空!");
            jsonResultModel.setReturnCode(403);
            return jsonResultModel;
        }else {
            monitorUserService.enabledMonitorUser(userId);
            return JsonResultModel.newJsonResultModel();
        }
    }

    @RequestMapping(value = "/disabled/super/user",method = RequestMethod.POST)
    public Object disabledSuperUser(@RequestParam  Long userId){
        if (Objects.isNull(userId)){
            JsonResultModel jsonResultModel = new JsonResultModel();
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("禁用失败,userId不能为空!");
            jsonResultModel.setReturnCode(403);
            return jsonResultModel;
        }else {
            monitorUserService.disabledMonitorUser(userId);
            return JsonResultModel.newJsonResultModel();
        }
    }

    @RequestMapping(value = "/change/super/user", method = RequestMethod.POST)
    public Object changeMonitor(@RequestParam(value = "userId")  Long userId,
                                @RequestParam(value = "classId")  Long classId,
                                @RequestParam(value = "classType")  String classType){
        return monitorUserService.changeMonitor(userId,classId,classType);
    }

    @RequestMapping(value = "/change/monitor/flag", method = RequestMethod.POST)
    public Object changeMonitorFlag(@RequestParam(value = "userId") Long userId,
                                  @RequestParam(value = "classId")  Long classId,
                                  @RequestParam(value = "classType")  String classType){
        monitorUserService.changeMonitorFlag(userId,classId,classType);
        return JsonResultModel.newJsonResultModel();
    }
}
