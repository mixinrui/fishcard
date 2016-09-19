package com.boxfishedu.workorder.service.absenteeism.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ansel on 16/9/14.
 */
@Service
public class AbsenteeismSDK {

    Logger logger = LoggerFactory.getLogger(AbsenteeismSDK.class);

    @Autowired
    UrlConf urlConf;

    @Autowired
    RestTemplate restTemplate;

    public JsonResultModel absenteeismDeductScore(WorkOrder workOrder){
        Map<String,String> paramsMap = new HashMap<>();
        paramsMap.put("lesson_id",workOrder.getCourseId());
        paramsMap.put("channel","online");
        paramsMap.put("message","{\"user_id\":"+workOrder.getStudentId()+",\"score\":30000}");
        paramsMap.put("type","ESCAPE");
        paramsMap.put("user_id",workOrder.getStudentId().toString());
        return restTemplate.postForObject(createDeductScoreURI(),paramsMap,JsonResultModel.class);
    }

    private URI createDeductScoreURI() {
        logger.info("Accessing createTeacherAbsenceURI in AbsenteeismSDK......");
        return UriComponentsBuilder.fromUriString(urlConf.getAbsenteeism_deduct_score())
                .path("/online/user/score/escape")
                .queryParam("")
                .build()
                .toUri();
    }
}
