package com.boxfishedu.workorder.service.monitor;

import com.boxfishedu.workorder.configuration.TeacherAppReleaseConf;
import com.boxfishedu.workorder.dao.jpa.*;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.servicex.monitor.MonitorUserServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    MonitorUserServiceX monitorUserServiceX;

    @Autowired
    TeacherAppReleaseConf teacherAppReleaseConf;

    @Autowired
    RestTemplate template;

    public List<MonitorUser> getAllSuperUser() {
        logger.info("@getAllSuperUser checking for login ...");
        return monitorUserJpaRepository.getEnabledUser();
    }

    public MonitorUser addMonitorUser(MonitorUserRequestForm monitorUserRequestForm) {
        logger.info("@addSuperUser adding monitor user ...");
        int min = 0;
        if (Objects.nonNull(monitorUserJpaRepository.getMinAvgSum())) {
            min = monitorUserJpaRepository.getMinAvgSum();
        }
        MonitorUser monitorUserNew = new MonitorUser(monitorUserRequestForm);
        monitorUserNew.setAvgSum(min);
        return monitorUserJpaRepository.save(monitorUserNew);
    }

    @Transactional
    public void enabledMonitorUser(Long userId) {
        logger.info("@enabledMonitorUser userId:[{}]", userId);
        monitorUserJpaRepository.enabledMonitorUser(new Date(), userId);
    }

    @Transactional
    public void disabledMonitorUser(Long userId) {
        logger.info("@disabledMonitorUser userId:[{}]", userId);
        monitorUserJpaRepository.disabledMonitorUser(new Date(), userId);
    }

    public MonitorUser updateUserInfo(MonitorUserRequestForm monitorUserRequestForm) {
        logger.info("@updateUserInfo update user info, monitorUserRequestForm:[{}]", monitorUserRequestForm);
        MonitorUser monitorUser = monitorUserJpaRepository.findByUserId(monitorUserRequestForm.getUserId());
        if (Objects.nonNull(monitorUserRequestForm.getUserName())) {
            monitorUser.setUserName(monitorUserRequestForm.getUserName());
        }
        if (Objects.nonNull(monitorUserRequestForm.getPassWord())) {
            monitorUser.setPassWord(monitorUserRequestForm.getPassWord());
        }
        if (Objects.nonNull(monitorUserRequestForm.getAccessToken())) {
            monitorUser.setAccessToken(monitorUserRequestForm.getAccessToken());
        }
        if (Objects.nonNull(monitorUserRequestForm.getUserType())) {
            monitorUser.setUserType(monitorUserRequestForm.getUserType());
        }
        monitorUser.setUpdateTime(new Date());
        return monitorUserJpaRepository.save(monitorUser);
    }

    public Page<MonitorResponseForm> page(String classType, Date startTime, Date endTime, Long userId, Pageable pageable) {
        logger.info("@page get class sum group by startTime ,userId:[{}]", userId);
        Page<MonitorResponseForm> monitorResponseFormPage = monitorUserCourseJpaRepository.getClassPage(classType, startTime, endTime, userId, pageable);
        //TODO 调用数据组接口,获取appRelease


        return monitorUserCourseJpaRepository.getClassPage(classType, startTime, endTime, userId, pageable);
    }

    public Object detailList(String classType, Date startTime, Date endTime, Long studentId, Pageable pageable) {
        logger.info("@detailList get class table ... studentId:[{}],classType:[{}]", studentId, classType);
        return smallClassJpaRepository.findMonitorUserCourse(startTime, endTime, classType, studentId, pageable);
    }

    @Transactional
    public void distributeClassToMonitor(SmallClass smallClass) {
        MonitorUser monitorUser = monitorUserJpaRepository.findTop1ByUserTypeAndEnabledOrderByAvgSum("student", 1);
        if (Objects.nonNull(monitorUser)) {
            logger.info("@distributeClassToMonitor distribute SmallClass:[{}] to userId:[{}]", smallClass, monitorUser.getUserId());
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
        } else {
            logger.info("@distributeClassToMonitor System does not have any monitor user!");
        }
    }

    public MonitorUser checkMonitorUser(Long userId) {
        logger.info("@checkMonitorUser userId:[{}]", userId);
        return monitorUserJpaRepository.findByUserIdAndEnabled(userId, 1);
    }

    @Transactional
    public JsonResultModel changeMonitor(Long userId, Long classId, String classType) {
        JsonResultModel jsonResultModel = new JsonResultModel();
        MonitorUser monitorUser = monitorUserJpaRepository.findByUserIdAndEnabled(userId, 1);
        if (Objects.isNull(monitorUser)) {
            jsonResultModel.setData(null);
            jsonResultModel.setReturnCode(403);
            jsonResultModel.setReturnMsg("用户无效,id:" + userId);
            return jsonResultModel;
        }
        List<MonitorUserCourse> monitorUserCourse = monitorUserCourseJpaRepository.findByClassIdAndClassType(classId, classType);
        if (Objects.isNull(monitorUserCourse)) {
            jsonResultModel.setData(null);
            jsonResultModel.setReturnMsg("小班课还未分配,请在小班课上课当天更换,smallClassId:" + classId);
            return jsonResultModel;
        }
        boolean status = monitorUserServiceX.deleteMoniorCourse(userId, classId);
        if (status) {
            monitorUserCourseJpaRepository.changeMonitor(monitorUser.getId(), userId, classId, classType);
            jsonResultModel.setData(null);
            jsonResultModel.setReturnCode(200);
            jsonResultModel.setReturnMsg("更换成功!");
            return jsonResultModel;
        } else {
            jsonResultModel.setData(null);
            jsonResultModel.setReturnCode(403);
            jsonResultModel.setReturnMsg("参数有误,请核对后操作,id:" + userId + ",smallClassId:" + classId);
            return jsonResultModel;
        }
    }

    @Transactional
    public void changeMonitorFlag(Long userId, Long classId, String classType) {
        logger.info("@changeMonitorFlag userId=[{}], classId=[{}], classType=[{}]", userId, classId, classType);
        monitorUserCourseJpaRepository.changeMonitorFlag(userId, classId, classType);
    }

    public TeacherAppReleaseForm getTeacherAppRelease(List userIdList) {
        Long time = System.currentTimeMillis();
//        Map paramMap = new HashMap();
//        paramMap.put("userIds", userIdList);
//        paramMap.put("time", time);
        String token = digest(userIdList.toString(), String.valueOf(time));
        Map header = new MultiValueMap();
        header.put("token", token);
        HttpEntity httpEntity = new HttpEntity(header);
        try {
            TeacherAppReleaseForm teacherAppReleaseForm = template.exchange(createTeacherAppReleaseURI(userIdList,time), HttpMethod.GET, httpEntity, TeacherAppReleaseForm.class).getBody();
            if (Objects.equals(teacherAppReleaseForm.getCode(), 0)) {
                return teacherAppReleaseForm;
            } else if (Objects.equals(teacherAppReleaseForm.getCode(), 1)) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("@getTeacherAppRelease 数据组或其他异常");
        }
        return null;
    }

    private URI createTeacherAppReleaseURI(List userIdsList, Long time) {
        logger.info("Accessing createTeacherAppReleaseURI in MonitorUserService......");
        org.springframework.util.MultiValueMap paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userIds",userIdsList);
        paramsMap.add("time",time);
        return UriComponentsBuilder.fromUriString(teacherAppReleaseConf.getGetReleaseUrl())
                .path("/app/log/v2/app/release/latest")
                .queryParams(paramsMap)
                .build()
                .toUri();
    }

    private String digest(String... args) {
        return DigestUtils.md5Hex(Arrays.stream(args).sorted(String::compareToIgnoreCase).collect(Collectors.joining()));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
