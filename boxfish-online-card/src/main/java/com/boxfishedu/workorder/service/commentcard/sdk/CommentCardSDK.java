package com.boxfishedu.workorder.service.commentcard.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
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

    private URI createTeacherAbsenceURI(){
        logger.info("Accessing createTeacherAbsenceURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString("http://192.168.88.210:8099")
                .path("/f_teacher_review/set_truant")
                .queryParam("")
                .build()
                .toUri();
    }

    private URI createGetPictureURI(String access_token){
        logger.info("Accessing createGetPictureURI in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString("http://114.55.58.184:8099")
                .path("/user/me")
                .queryParam("access_token",access_token)
                .build()
                .toUri();
    }
}
