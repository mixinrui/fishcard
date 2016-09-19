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
        return restTemplate.getForObject(createDeductScoreURI(workOrder),JsonResultModel.class);
    }

    private URI createDeductScoreURI(WorkOrder workOrder) {
        logger.info("Accessing createTeacherAbsenceURI in AbsenteeismSDK......");
        MultiValueMap paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("lesson_id",workOrder.getCourseId());
        paramsMap.add("channel","online");
        paramsMap.add("message","user_id:"+workOrder.getStudentId()+",score:30000");
        paramsMap.add("type","ESCAPE");
        paramsMap.add("user_id",workOrder.getStudentId());
        return UriComponentsBuilder.fromUriString(urlConf.getAbsenteeism_deduct_score())
                .path("/statistic/user/score")
                .queryParams(paramsMap)
                .build()
                .toUri();
    }
}
