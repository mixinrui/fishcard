package com.boxfishedu.workorder.service.monitor;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.MonitorUserJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import com.boxfishedu.workorder.entity.mysql.MonitorUserRequestForm;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.servicex.bean.StudentCourseSchedule;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    CourseScheduleService courseScheduleService;

    @Autowired
    ServiceSDK serviceSDK;

    @Autowired
    TeacherStudentRequester teacherStudentRequester;

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

    public Object detailList(Long userId, Pageable pageable,Locale locale){

        Page<CourseSchedule> page = courseScheduleService.findByStudentId(userId, pageable);
        List<Map<String, Object>> result = adapterCourseScheduleLists(page.getContent(), locale);
        HashMap<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("data", result);
        resultMap.put("returnCode", HttpStatus.SC_OK);
        resultMap.put("totalElements", page.getTotalElements());
        resultMap.put("number", page.getNumber());
        resultMap.put("totalPages", page.getTotalPages());
        resultMap.put("size", page.getSize());
        return resultMap;
    }

    private List<Map<String, Object>> adapterCourseScheduleLists(List<CourseSchedule> courseScheduleList, Locale locale) {
        Map<String, List<StudentCourseSchedule>> courseScheduleMap = Maps.newLinkedHashMap();
        Date now = new Date();
        courseScheduleList.forEach(courseSchedule -> {
            String date = DateUtil.simpleDate2String(courseSchedule.getClassDate());
            courseScheduleMap.compute(date, (k, v) -> {
                if(v == null) {
                    v = Lists.newArrayList();
                }
                v.add(createStudentCourseSchedule(courseSchedule, locale,now));
                return v;
            });
        });

        List<Map<String, Object>> result = Lists.newArrayList();
        courseScheduleMap.forEach((key, val) -> {
            Map<String, Object> beanMap = Maps.newHashMap();
            beanMap.put("day", key);
            beanMap.put("dailyScheduleTime", val);
            result.add(beanMap);
        });
        return result;
    }

    private StudentCourseSchedule createStudentCourseSchedule(CourseSchedule courseSchedule, Locale locale,Date now) {
        TimeSlots timeSlots = getTimeSlotById(courseSchedule.getTimeSlotId());
        StudentCourseSchedule studentCourseSchedule = new StudentCourseSchedule();
        studentCourseSchedule.setId(courseSchedule.getId());
        studentCourseSchedule.setCourseId(courseSchedule.getCourseId());

        if (courseSchedule.getStartTime().after(now) && Objects.equals(1, courseSchedule.getIsFreeze())) {
            studentCourseSchedule.setNeedChangeTime(courseSchedule.getNeedChangeTime());/** 显示需要修改时间的鱼卡信息 **/
        } else {
            studentCourseSchedule.setNeedChangeTime(null);
        }
        studentCourseSchedule.setCourseType(courseSchedule.getCourseType());
        if(StringUtils.equals(ClassTypeEnum.INSTNAT.toString(),courseSchedule.getClassType())){
            studentCourseSchedule.setTime(courseSchedule.getInstantStartTtime());
        }
        else {
            studentCourseSchedule.setTime(timeSlots.getStartTime());
        }
        studentCourseSchedule.setWorkOrderId(courseSchedule.getWorkorderId());
        studentCourseSchedule.setStatus(courseSchedule.getStatus());
        studentCourseSchedule.setIsFreeze(courseSchedule.getIsFreeze());
        studentCourseSchedule.setClassType(courseSchedule.getClassType());
        studentCourseSchedule.setSmallClassId(courseSchedule.getSmallClassId());
        if (StringUtils.isNotEmpty(courseSchedule.getCourseId())) {
            studentCourseSchedule.setCourseView(serviceSDK.getCourseInfoByScheduleId(courseSchedule.getId(), locale));
        }
        return studentCourseSchedule;
    }

    public TimeSlots getTimeSlotById(Integer id) throws BusinessException {
        return teacherStudentRequester.getTimeSlot(id);
    }
}
