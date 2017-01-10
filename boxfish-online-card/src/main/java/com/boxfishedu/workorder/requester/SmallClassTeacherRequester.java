package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by hucl on 17/1/7.
 */
@Component
public class SmallClassTeacherRequester {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private CacheManager cacheManager;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String ONETOONE = "ONE_TO_ONE_ONLINE";
    private final String SMALL_CLASS = "SMALL_CLASS";
    private final String PUBLIC_CLASS = "PUBLIC_CLASS";

    public TeacherView getSmallClassTeacher(SmallClass smallClass) {
        SmallClassFetchTeacherParam smallClassFetchTeacherParam = new SmallClassFetchTeacherParam();
        smallClassFetchTeacherParam.setDay(smallClass.getClassDate().getTime());
        smallClassFetchTeacherParam.setSlotId(smallClass.getSlotId().longValue());
        smallClass.setRoleId(smallClass.getRoleId());
        smallClass.setSmallClassType(SMALL_CLASS);

        String url = String.format("%s/course/schedule/teacher/web/match", urlConf.getTeacher_service());
        restTemplate.postForObject(url, smallClassFetchTeacherParam, JsonResultModel.class);
        return null;
    }

    //获取公开课
    public TeacherView getPublicTeacher(SmallClass smallClass) {
        return null;
    }

    @Data
    public static class SmallClassFetchTeacherParam {
        private Long day;
        private String courseType;
        private Long slotId;
        private Integer roleId;
        //ONE_TO_ONE_ONLINE, 在线授课一对一   SMALL_CLASS,//小班课   PUBLIC_CLASS//公开课
        private String classType;
    }
}
