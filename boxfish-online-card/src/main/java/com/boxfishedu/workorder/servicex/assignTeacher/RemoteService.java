package com.boxfishedu.workorder.servicex.assignTeacher;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
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
        //urlConf.getTeacher_service().trim()
        JsonResultModel jsonResultModel = restTemplate.postForObject(urlConf.getTeacher_service()+"/course/schedule/applyDesignatedTeacher",reqSt,JsonResultModel.class);
//        JsonResultModel jsonResultModel = restTemplate.postForObject("http://192.168.55.240:8099/teacher/course/schedule/applyDesignatedTeacher",reqSt,JsonResultModel.class);
        if(null == jsonResultModel){
            throw new BusinessException("请求师生运营系统匹配老师异常");
        }
        if(jsonResultModel.getReturnCode() != 200){
            throw new BusinessException("请求师生运营系统匹配老师异常");
        }
        ScheduleBatchReqSt scheduleBatchReqSt = jsonResultModel.getData(ScheduleBatchReqSt.class);
        return scheduleBatchReqSt;
    }

}
