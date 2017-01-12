package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
        smallClass.setClassType(SMALL_CLASS);

        String url = String.format("%s/course/schedule/teacher/web/match", urlConf.getTeacher_service());
        JsonResultModel jsonResultModel = null;
        try {
            jsonResultModel = restTemplate.postForObject(url, smallClassFetchTeacherParam, JsonResultModel.class);
            logger.debug("@getSmallClassTeacher#获取小班课教师成功,url[{}],参数[{}],结果[{}]"
                    , url, smallClassFetchTeacherParam, JacksonUtil.toJSon(jsonResultModel));
        } catch (Exception ex) {
            logger.error("@getSmallClassTeacher#获取小班课教师失败,url[{}],参数[{}],结果[{}]"
                    , url, smallClassFetchTeacherParam, JacksonUtil.toJSon(jsonResultModel));
            return null;
        }
        TeacherView teacherView = jsonResultModel.getData(TeacherView.class);
        return teacherView;
    }

    //获取公开课
    public TeacherView getPublicTeacher(SmallClass smallClass) {
        return null;
    }

    public void assignPublicClassTeacher(SmallClass smallClass) {
        String url = String.format("%s/course/schedule/teacher/public/confirm", urlConf.getTeacher_service());
        Map<String, Object> param = Maps.newHashMap();
        param.put("day", smallClass.getClassDate().getTime());
        param.put("timeSlotId", smallClass.getSlotId());
        param.put("teacherId", smallClass.getTeacherId());
        param.put("studentId", 0);

        JsonResultModel jsonResultModel = null;
        try{
            jsonResultModel=restTemplate.postForObject(url,param,JsonResultModel.class);
            if(jsonResultModel.getReturnCode()== HttpStatus.SC_OK){
                logger.debug("@assignPublicClassTeacher#分配教师成功,url[{}],param[{}],结果[{}]"
                        ,url,param,JacksonUtil.toJSon(jsonResultModel));
            }
            else{
                throw new BusinessException("公开课分配教师失败");
            }
        }
        catch (Exception ex){
            logger.error("@assignPublicClassTeacher#分配教师失败,url[{}],param[{}],结果[{}]"
                    ,url,param,JacksonUtil.toJSon(jsonResultModel));
        }
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
