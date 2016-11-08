package com.boxfishedu.workorder.requester;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.service.instantclass.InstantRecommandAlthom;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import lombok.Data;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
}
