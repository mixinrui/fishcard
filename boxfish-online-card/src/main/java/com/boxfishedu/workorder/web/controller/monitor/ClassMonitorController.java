package com.boxfishedu.workorder.web.controller.monitor;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.MonitorResponseForm;
import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by ansel on 2017/3/16.
 */
@RestController
@RequestMapping(value = "/class/monitor")
public class ClassMonitorController {

    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

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

        //mock
        MonitorUser monitorUser1 = new MonitorUser();
        monitorUser1.setUserId(123456l);
        MonitorUser monitorUser2 = new MonitorUser();
        monitorUser2.setUserId(567890l);
        List list = new ArrayList();
        list.add(monitorUser1);
        list.add(monitorUser2);
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData(list);
        return jsonResultModel;
    }

}
