package com.boxfishedu.workorder.service.monitor;

import com.boxfishedu.workorder.dao.jpa.MonitorUserCourseJpaRepository;
import com.boxfishedu.workorder.dao.jpa.MonitorUserJpaRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by ansel on 2017/3/21.
 */
@Service
public class MonitorUserService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MonitorUserJpaRepository monitorUserJpaRepository;

    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    MonitorUserCourseJpaRepository monitorUserCourseJpaRepository;


    public List<MonitorUser> getAllSuperUser(){
        logger.info("@getAllSuperUser checking for login ...");
        return monitorUserJpaRepository.getEnabledUser();
    }

    public MonitorUser addMonitorUser(MonitorUserRequestForm monitorUserRequestForm){
        logger.info("@addSuperUser adding monitor user ...");
        int min = 0;
        if (Objects.nonNull(monitorUserJpaRepository.getMinAvgSum())){
            min = monitorUserJpaRepository.getMinAvgSum();
        }
        MonitorUser monitorUserNew = new MonitorUser(monitorUserRequestForm);
        monitorUserNew.setAvgSum(min);
        return monitorUserJpaRepository.save(monitorUserNew);
    }

    @Transactional
    public void enabledMonitorUser(Long userId){
        logger.info("@enabledMonitorUser userId:[{}]",userId);
        monitorUserJpaRepository.enabledMonitorUser(new Date(),userId);
    }

    @Transactional
    public void disabledMonitorUser(Long userId){
        logger.info("@disabledMonitorUser userId:[{}]",userId);
        monitorUserJpaRepository.disabledMonitorUser(new Date(),userId);
    }

    public MonitorUser updateUserInfo(MonitorUserRequestForm monitorUserRequestForm){
        logger.info("@updateUserInfo update user info, monitorUserRequestForm:[{}]",monitorUserRequestForm);
        MonitorUser monitorUser = monitorUserJpaRepository.findByUserId(monitorUserRequestForm.getUserId());
        if (Objects.nonNull(monitorUserRequestForm.getUserName())){
            monitorUser.setUserName(monitorUserRequestForm.getUserName());
        }
        if (Objects.nonNull(monitorUserRequestForm.getPassWord())){
            monitorUser.setPassWord(monitorUserRequestForm.getPassWord());
        }
        if (Objects.nonNull(monitorUserRequestForm.getAccessToken())){
            monitorUser.setAccessToken(monitorUserRequestForm.getAccessToken());
        }
        if (Objects.nonNull(monitorUserRequestForm.getUserType())){
            monitorUser.setUserType(monitorUserRequestForm.getUserType());
        }
        monitorUser.setUpdateTime(new Date());
        return monitorUserJpaRepository.save(monitorUser);
    }

    public Page<MonitorResponseForm> page(String classType,Date startTime,Date endTime,Long userId,Pageable pageable){
        logger.info("@page get class sum group by startTime ,userId:[{}]",userId);
        return monitorUserCourseJpaRepository.getClassPage(classType,startTime,endTime,userId,pageable);
    }

    public Object detailList(String classType, Date startTime, Date endTime,Long studentId,Pageable pageable){
        logger.info("@detailList get class table ... studentId:[{}],classType:[{}]",studentId,classType);
        return smallClassJpaRepository.findMonitorUserCourse(startTime,endTime,classType,studentId,pageable);
    }

    @Transactional
    public void distributeClassToMonitor(SmallClass smallClass){
        MonitorUser monitorUser = monitorUserJpaRepository.findTop1ByUserTypeAndEnabledOrderByAvgSum("student",1);
        if (Objects.nonNull(monitorUser)){
            logger.info("@distributeClassToMonitor distribute SmallClass:[{}] to userId:[{}]",smallClass,monitorUser.getUserId());
            MonitorUserCourse monitorUserCourse = new MonitorUserCourse();
            monitorUserCourse.setMonitorUserId(monitorUser.getId());
            monitorUserCourse.setUserId(monitorUser.getUserId());
            monitorUserCourse.setClassId(smallClass.getId());
            monitorUserCourse.setClassType(smallClass.getClassType());
            monitorUserCourse.setCourseId(smallClass.getCourseId());
            monitorUserCourse.setStartTime(smallClass.getStartTime());
            monitorUserCourse.setEndTime(smallClass.getEndTime());
            monitorUserCourse.setCreateTime(new Date());
            monitorUserCourseJpaRepository.save(monitorUserCourse);
            monitorUserJpaRepository.updateAvgSum(monitorUser.getId());
        }else {
            logger.info("@distributeClassToMonitor System does not have any monitor user!");
        }
    }

    public MonitorUser checkMonitorUser(Long userId){
        logger.info("@checkMonitorUser userId:[{}]",userId);
        return monitorUserJpaRepository.findByUserIdAndEnabled(userId,1);
    }
}
