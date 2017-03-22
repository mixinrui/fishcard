package com.boxfishedu.workorder.service.monitor;

import com.boxfishedu.workorder.dao.jpa.MonitorUserJpaRepository;
import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import com.boxfishedu.workorder.entity.mysql.MonitorUserRequestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by ansel on 2017/3/21.
 */
@Service
public class MonitorUserService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    MonitorUserJpaRepository monitorUserJpaRepository;

    public List<MonitorUser> getAllSuperUser(){
        logger.info("@getAllSuperUser checking for login ...");
        return monitorUserJpaRepository.getEnabledUser();
    }

    public MonitorUser addMonitorUser(MonitorUserRequestForm monitorUserRequestForm){
        logger.info("@addSuperUser adding monitor user ...");
        MonitorUser monitorUser = new MonitorUser(monitorUserRequestForm);
        return monitorUserJpaRepository.save(monitorUser);
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
}
