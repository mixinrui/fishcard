package com.boxfishedu.workorder.service.absenteeism.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.config.UrlConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by ansel on 16/9/14.
 */
@Service
public class AbsenteeismSDK {

    @Autowired
    Logger logger = LoggerFactory.getLogger(AbsenteeismSDK.class);

    @Autowired
    UrlConf urlConf;

    @Autowired
    RestTemplate restTemplate;

    public JsonResultModel absenteeismDeductScore(Long studentId, long score){
        return restTemplate.getForObject(createDeductScoreURI(studentId,score),JsonResultModel.class);
    }

    private URI createDeductScoreURI(Long studentId,long score) {
        logger.info("Accessing createTeacherAbsenceURI in AbsenteeismSDK......");
        return UriComponentsBuilder.fromUriString(urlConf.getAbsenteeism_deduct_score())
                .path("")
                .queryParam("studentId",studentId,"score",score)
                .build()
                .toUri();
    }
}
