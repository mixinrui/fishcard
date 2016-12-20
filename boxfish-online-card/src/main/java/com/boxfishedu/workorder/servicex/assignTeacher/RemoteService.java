package com.boxfishedu.workorder.servicex.assignTeacher;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by olly on 2016/12/20.
 */
@Component
public class RemoteService {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    UrlConf urlConf;
    public ScheduleBatchReqSt matchTeacher(ScheduleBatchReqSt reqSt){
        return restTemplate.postForObject(urlConf.getTeacher_service().trim()+"/course/schedule/applyDesignatedTeacher",reqSt,ScheduleBatchReqSt.class);
    }

}
