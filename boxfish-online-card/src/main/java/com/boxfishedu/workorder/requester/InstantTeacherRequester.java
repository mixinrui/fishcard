package com.boxfishedu.workorder.requester;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.service.instantclass.InstantRecommandAlthom;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by hucl on 16/11/7.
 */
@Component
public class InstantTeacherRequester {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;


    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    //学生发起请求以后,获取可用的教师列表
    public Optional<List<Long>> getInstantTeacherIds(InstantClassCard instantClassCard){
        InstantFetchTeacherParam instantFetchTeacherParam=new InstantFetchTeacherParam();
        instantFetchTeacherParam.setDay(instantClassCard.getClassDate().getTime());
        instantFetchTeacherParam.setCourseType(instantClassCard.getCourseType());
        //TODO:这里暂时假数据,到时候会替换
//        InstantRecommandAlthom instantRecommandAlthom=new InstantRecommandAlthom(instantClassCard.getRequestTeacherTimes());
//        instantFetchTeacherParam.setCountStart(instantRecommandAlthom.getCountStart());
//        instantFetchTeacherParam.setCountEnd(instantRecommandAlthom.getCountEnd());

        instantFetchTeacherParam.setCountStart(0);
        instantFetchTeacherParam.setCountEnd(50);
        instantFetchTeacherParam.setRoleId(instantClassCard.getRoleId());
        instantFetchTeacherParam.setSlotId(instantClassCard.getSlotId());
        return this.parseFetchTeachers(this.getInstantTeachers(instantFetchTeacherParam));
    }


    //向师生运营发起获取教师请求
    public InstantAssignTeacher assignGrabteacher(InstantClassCard instantClassCard, TeacherInstantRequestParam teacherInstantRequestParam){
        InstantAssignTeacherParam instantAssignTeacherParam=new InstantAssignTeacherParam();
        instantAssignTeacherParam.setDay(instantClassCard.getClassDate().getTime());
        instantAssignTeacherParam.setSlotId(instantClassCard.getSlotId());
        instantAssignTeacherParam.setStudentId(instantClassCard.getStudentId());
        instantAssignTeacherParam.setTeacherId(teacherInstantRequestParam.getTeacherId());
        return this.assignGrabteacher(instantAssignTeacherParam);
    }

    private InstantAssignTeacher assignGrabteacher(InstantAssignTeacherParam instantAssignTeacherParam){
        String url = String.format("%s/immediately/course/schedule/online/grab", urlConf.getTeacher_service());
        logger.debug(">>>>>>>>>>>@assignGrabteacher===>>>>教师抢课以后开始前往师生运营校验#url:{}#param:{}"
                ,url, JacksonUtil.toJSon(instantAssignTeacherParam));
        JsonResultModel jsonResultModel=null;
        try{
            jsonResultModel = restTemplate.postForObject(url, instantAssignTeacherParam, JsonResultModel.class);
            if(jsonResultModel.getReturnCode()!= HttpStatus.OK.value()){
                throw new BusinessException("向师生运营发起请求获取失败"+jsonResultModel.getReturnMsg());
            }
        }
        catch (Exception ex){
            logger.error("!!!!!!!@IassignGrabteacher#前往师生运营校验失败失败,url{},参数[{}]",url,JacksonUtil.toJSon(instantAssignTeacherParam),ex);
            throw new BusinessException("教师校验失败");
        }
        logger.debug("<<<<<<<<<<<<@assignGrabteacher===>>>>校验成功,返回值[{}]",jsonResultModel);
        return jsonResultModel.getData(InstantAssignTeacher.class);
    }

    private JsonResultModel getInstantTeachers(InstantFetchTeacherParam fetchTeacherParam) {
        String url = String.format("%s/immediately/course/schedule/online/getRecommendImmediatelyTeachers", urlConf.getTeacher_service());
        logger.debug(">>>>>>>>>>>@InstantTeacherRequester#getInstantTeachers#url:{}#param:{}"
                ,url, JacksonUtil.toJSon(fetchTeacherParam));
        JsonResultModel jsonResultModel=null;
        try{
            jsonResultModel = restTemplate.postForObject(url, fetchTeacherParam, JsonResultModel.class);
        }
        catch (Exception ex){
            logger.error("!!!!!!!@InstantTeacherRequester#getInstantTeachers失败,url{},参数[{}]",url,JacksonUtil.toJSon(fetchTeacherParam),ex);
            throw new BusinessException("获取实时推荐教师失败");
        }
        return jsonResultModel;
    }

    private Optional<List<Long>> parseFetchTeachers(JsonResultModel jsonResultModel){
        List<Long> teachers= jsonResultModel.getListData(Long.class);
        return Optional.ofNullable(teachers);
    }

    @Data
    class InstantFetchTeacherParam{
        private Long day;
        private String courseType;
        private Long slotId;
        private Integer roleId;
        private Integer countStart;
        private Integer countEnd;
    }

    @Data
    class InstantAssignTeacherParam{
        private Long day;
        private Long slotId;
        private Long teacherId;
        private Long studentId;
    }

    @Data
    public static class InstantAssignTeacher{
        private Long teacherId;
        private String teacherName;
        private Boolean select;
    }
}
