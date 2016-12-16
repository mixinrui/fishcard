package com.boxfishedu.workorder.requester;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.FetchTeacherParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.TokenReturnBean;
import com.boxfishedu.workorder.web.view.teacher.PlannerAssignView;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

/**
 * Created by hucl on 16/6/17.
 */
@Component
public class TeacherPhotoRequester {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取教师的图像
     * @param teacherId
     * @return
     */
    public String getTeacherPhoto(Long teacherId) {
        String url = String.format("%s/%s", urlConf.getTeacher_photo(), teacherId);
        try {
            logger.info("获取教师图像,url:[{}]", url);
            Map<String, String> map = restTemplate.getForObject(url, Map.class);
            return map.get("figure_url").toString();
        } catch (Exception ex) {
            logger.error("获取教师图像失败,url[{}]",url, ex);
            return null;
        }
    }

    /**
     * 获取老师信息
     * @param teacherId
     * @return  figure_url
     */
    public Map<String, String> getTeacherInfo(Long teacherId) {
        String url = String.format("%s/%s", urlConf.getTeacher_photo(), teacherId);
        try {
            logger.info("获取教师xinxi ,url:[{}]", url);
            Map<String, String> map = restTemplate.getForObject(url, Map.class);
            return map;
        } catch (Exception ex) {
            logger.error("获取教师失败,url[{}]",url, ex);
            return null;
        }
    }

    /**
     * 获取课程信息

     "score": 2770,
     "readWordCount": 154,
     "multiwordCount": 46

     * @param courseId
     * @return
     */
    public Map<String,Integer> getCourseInfo(String courseId){

        String url = String.format("%s/%s/%s", urlConf.getResource_url(),"course/info", courseId);
        try {
            logger.info("获取课程信息,url:[{}]", url);
            Map<String, Integer> map = restTemplate.getForObject(url, Map.class);
            return map;
        } catch (Exception ex) {
            logger.error("获取课程信息,url[{}]",url, ex);
            return null;
        }
    }


}
