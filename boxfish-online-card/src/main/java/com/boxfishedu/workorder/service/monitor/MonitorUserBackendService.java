package com.boxfishedu.workorder.service.monitor;

import com.boxfishedu.workorder.dao.jpa.MonitorUserCourseJpaRepository;
import com.boxfishedu.workorder.dao.jpa.MonitorUserJpaRepository;
import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import com.boxfishedu.workorder.entity.mysql.MonitorUserCourse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Created by ansel on 2017/4/10.
 */
@Service
public class MonitorUserBackendService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MonitorUserCourseJpaRepository monitorUserCourseJpaRepository;

    @Autowired
    private MonitorUserJpaRepository monitorUserJpaRepository;

    public MonitorUserCourse getMonitorCourse(Long classId, String classType){
        logger.info("@getMonitorCourse classId=[{}], classType=[{}]", classId, classType);
        return monitorUserCourseJpaRepository.getByClassIdAndClassType(classId,classType);
    }

    public Object getMonitorList(Long classId, String classType, Pageable pageable){
        logger.info("@getMonitorList classId=[{}], classType=[{}]", classId, classType);
        if (Objects.nonNull(classId)){
            MonitorUserCourse monitorUserCourse = getMonitorCourse(classId,classType);
            return monitorUserJpaRepository.monitorBackendGetUserList(monitorUserCourse.getUserId(),pageable);
        } else {
            return monitorUserJpaRepository.getEnabledUserPage(pageable);
        }
    }
}
