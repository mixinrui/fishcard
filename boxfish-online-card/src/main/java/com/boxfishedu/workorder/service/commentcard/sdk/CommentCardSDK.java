package com.boxfishedu.workorder.service.commentcard.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by ansel on 16/7/26.
 */
@Service
public class CommentCardSDK {

    private Logger logger = LoggerFactory.getLogger(CommentCardSDK.class);

    @Autowired
    private RestTemplate restTemplate;

    public JsonResultModel setTeacherAbsence(Long teacherId, Long id){
        return restTemplate.getForObject(createTeacherAbsenceURI(teacherId,id),JsonResultModel.class);
    }

    private URI createTeacherAbsenceURI(Long teacherId,Long id){
        logger.info("Accessing createTeacherAbsenceURI in CommentCardSDK......");
        MultiValueMap<String,String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("teacherId",teacherId.toString());
        paramMap.add("fishCardId",id.toString());
        return UriComponentsBuilder.fromUriString("http://192.168.88.210:8099")
                .path("/f_teacher_review/set_truant")
                .queryParams(paramMap)
                .build()
                .toUri();
    }
}
