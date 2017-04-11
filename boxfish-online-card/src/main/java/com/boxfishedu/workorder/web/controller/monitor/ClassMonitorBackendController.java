package com.boxfishedu.workorder.web.controller.monitor;

import com.boxfishedu.workorder.dao.jpa.MonitorUserJpaRepository;
import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import com.boxfishedu.workorder.entity.mysql.MonitorUserCourse;
import com.boxfishedu.workorder.entity.mysql.MonitorUserRequestForm;
import com.boxfishedu.workorder.service.monitor.MonitorUserBackendService;
import com.boxfishedu.workorder.service.monitor.MonitorUserService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ansel on 2017/4/10.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/backend/monitor")
public class ClassMonitorBackendController {

    @Autowired
    MonitorUserBackendService monitorUserBackendService;

    @Autowired
    MonitorUserService monitorUserService;

    @Autowired
    MonitorUserJpaRepository monitorUserJpaRepository;

    @RequestMapping(value = "/get/monitor_course", method = RequestMethod.GET)
    public Object getMonitorCourse(@RequestParam(value = "classId") Long classId, @RequestParam(value = "classType") String classType){
        MonitorUserCourse monitorUserCourse = monitorUserBackendService.getMonitorCourse(classId,classType);
        JsonResultModel jsonResultModel = new JsonResultModel();
        if (Objects.isNull(monitorUserCourse)){
            return jsonResultModel;
        }else {
            MonitorUser monitorUser = monitorUserJpaRepository.findByUserId(monitorUserCourse.getUserId());
            Map requestMap = new HashMap();
            requestMap.put("classId",monitorUserCourse.getClassId());
            requestMap.put("classType",monitorUserCourse.getClassType());
            requestMap.put("userId",monitorUserCourse.getUserId());
            requestMap.put("userName",monitorUser.getUserName());
            requestMap.put("userType",monitorUser.getUserType());
            requestMap.put("enabled",monitorUser.getEnabled());
            jsonResultModel.setData(requestMap);
            return jsonResultModel;
        }
    }

    @RequestMapping(value = "/get/monitor_listitem", method = RequestMethod.GET)
    public Object getMonitorList(@RequestParam(value = "classId") Long classId,
                                 @RequestParam(value = "classType") String classType,Pageable pageable){
        Object monitorUserList = monitorUserBackendService.getMonitorList(classId,classType,pageable);
        JsonResultModel jsonResultModel = new JsonResultModel();
        if (Objects.isNull(monitorUserList)){
            return jsonResultModel;
        }else {
            jsonResultModel.setData(monitorUserList);
            return jsonResultModel;
        }
    }

    @RequestMapping(value = "/change/super/user", method = RequestMethod.POST)
    public Object changeMonitor(@RequestParam(value = "userId")  Long userId,
                                @RequestParam(value = "classId")  Long classId,
                                @RequestParam(value = "classType")  String classType){
        return monitorUserService.changeMonitor(userId,classId,classType);
    }

    @RequestMapping(value = "/add/super/user",method = RequestMethod.POST)
    public Object addSuperUser(@RequestBody MonitorUserRequestForm monitorUserRequestForm){
        if (Objects.isNull(monitorUserRequestForm.getUserId())){
            com.boxfishedu.beans.view.JsonResultModel jsonResultModel = new com.boxfishedu.beans.view.JsonResultModel();
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("添加失败,userId不能为空!");
            jsonResultModel.setReturnCode(403);
            return jsonResultModel;
        }else {
            return com.boxfishedu.beans.view.JsonResultModel.newJsonResultModel(monitorUserService.addMonitorUser(monitorUserRequestForm));
        }
    }

    @RequestMapping(value = "/update/super/user",method = RequestMethod.POST)
    public Object updateSuperUser(@RequestBody MonitorUserRequestForm monitorUserRequestForm){
        if (Objects.isNull(monitorUserRequestForm.getUserId())){
            com.boxfishedu.beans.view.JsonResultModel jsonResultModel = new com.boxfishedu.beans.view.JsonResultModel();
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("修改失败,userId不能为空!");
            jsonResultModel.setReturnCode(403);
            return jsonResultModel;
        }else {
            return com.boxfishedu.beans.view.JsonResultModel.newJsonResultModel(monitorUserService.updateUserInfo(monitorUserRequestForm));
        }

    }

    @RequestMapping(value = "/enabled/super/user",method = RequestMethod.POST)
    public Object enabledSuperUser(@RequestParam  Long userId){
        if (Objects.isNull(userId)){
            com.boxfishedu.beans.view.JsonResultModel jsonResultModel = new com.boxfishedu.beans.view.JsonResultModel();
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("激活失败,userId不能为空!");
            jsonResultModel.setReturnCode(403);
            return jsonResultModel;
        }else {
            monitorUserService.enabledMonitorUser(userId);
            return com.boxfishedu.beans.view.JsonResultModel.newJsonResultModel();
        }
    }

    @RequestMapping(value = "/disabled/super/user",method = RequestMethod.POST)
    public Object disabledSuperUser(@RequestParam  Long userId){
        if (Objects.isNull(userId)){
            com.boxfishedu.beans.view.JsonResultModel jsonResultModel = new com.boxfishedu.beans.view.JsonResultModel();
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("禁用失败,userId不能为空!");
            jsonResultModel.setReturnCode(403);
            return jsonResultModel;
        }else {
            monitorUserService.disabledMonitorUser(userId);
            return com.boxfishedu.beans.view.JsonResultModel.newJsonResultModel();
        }
    }

}
