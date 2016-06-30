package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by hucl on 16/6/12.
 */
@Component
public class RecommandedCourseService {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;

    private Logger logger= LoggerFactory.getLogger(this.getClass());


    public Integer getCourseIndex(WorkOrder workOrder){
        if(workOrder.getSeqNum()%8==0){
            return 8;
        }
        else{
            return workOrder.getSeqNum()%8;
        }
    }
}
