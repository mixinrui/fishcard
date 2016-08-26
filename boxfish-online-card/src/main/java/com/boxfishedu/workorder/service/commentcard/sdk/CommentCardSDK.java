package com.boxfishedu.workorder.service.commentcard.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.config.CommentCardUrlConf;
import com.boxfishedu.workorder.entity.mysql.PushToStudentAndTeacher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ansel on 16/7/26.
 */
@Service
public class CommentCardSDK {

    private Logger logger = LoggerFactory.getLogger(CommentCardSDK.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CommentCardUrlConf commentCardUrlConf;

    public JsonResultModel setTeacherAbsence(Long teacherId,Long studentId, Long id){
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("teacherId",teacherId.toString());
        paramMap.put("studentId",studentId.toString());
        paramMap.put("fishCardId",id.toString());
        return restTemplate.postForObject(createTeacherAbsenceURI(), paramMap,JsonResultModel.class);
    }

    public String getUserPicture(String access_token){
        return restTemplate.getForObject(createGetPictureURI(access_token),String.class);
    }

    public JsonResultModel pushToStudentAndTeacher(Long userId, String title, String type){
        PushToStudentAndTeacher pushToStudentAndTeacher = new PushToStudentAndTeacher();
        pushToStudentAndTeacher.setUser_id(userId);
        pushToStudentAndTeacher.setPush_title(title);
        Map<String,String> map = new HashMap<>();
        map.put("type",type);
        pushToStudentAndTeacher.setData(map);
        return restTemplate.postForObject(createPushURI(), Arrays.asList(pushToStudentAndTeacher),JsonResultModel.class);
    }

    public JsonResultModel getInnerTeacherId(Map paramMap){
        return restTemplate.postForObject(createTeacherAbsenceURI(), paramMap,JsonResultModel.class);
    }

    private URI createTeacherAbsenceURI(){
        logger.info("Accessing createTeacherAbsenceURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getTeacherAbsenceUrl())
                .path("/f_teacher_review/set_truant")
                .queryParam("")
                .build()
                .toUri();
    }

    private URI createGetPictureURI(String access_token){
        logger.info("Accessing createGetPictureURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getGetPictureUrl())
                .path("/user/me")
                .queryParam("access_token",access_token)
                .build()
                .toUri();
    }

    private URI createPushURI(){
        logger.info("Accessing createPushURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getPushInfoIrl())
                .path("/teaching/callback/push")
                .queryParam("")
                .build()
                .toUri();
    }

    private URI getInnerTeacherURI(){
        logger.info("Accessing getInnerTeacher in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getInnerTeacherUrl())
                .path("/f_teacher_review/get_inner_f_review_teacher")
                .queryParam("")
                .build()
                .toUri();
    }
}
