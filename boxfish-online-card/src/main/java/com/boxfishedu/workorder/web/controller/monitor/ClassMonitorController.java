package com.boxfishedu.workorder.web.controller.monitor;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.MonitorResponseForm;
import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import com.boxfishedu.workorder.entity.mysql.MonitorUserRequestForm;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.monitor.MonitorUserService;
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
    SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    MonitorUserService monitorUserService;

    @RequestMapping(value = "/page", method = RequestMethod.GET)
    public Object page(String classType,Date startTime,Date endTime,Pageable pageable,Long userId){

        //mock
        Map map = new HashMap();
        List monitorList = new ArrayList();
        map.put("page",1);
        map.put("size",2);
        monitorList.add(new MonitorResponseForm(new Date(),new Date(),20));
        monitorList.add(new MonitorResponseForm(new Date(),new Date(),13));
        map.put("content",monitorList);
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData(map);
        return jsonResultModel;
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public Object detail(String classType,Pageable pageable, Date startTime, Date endTime, Long userId){



        //mock
        SmallClass smallClass = new SmallClass();
        List<SmallClass> listSmallClass = smallClassJpaRepository.findMockData();
        Map map = new HashMap();
        map.put("page",1);
        map.put("size",9);
        map.put("totalPages",2);
        map.put("totalElements",19);
        map.put("dailyScheduleTime",listSmallClass);
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData(map);
        return jsonResultModel;
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

}
