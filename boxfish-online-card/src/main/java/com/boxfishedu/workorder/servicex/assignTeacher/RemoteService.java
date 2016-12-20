package com.boxfishedu.workorder.servicex.assignTeacher;

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
    @Value("${match_teacher_url}")
    private String matchTeacherUrl;

   // ScheduleBatchReqSt scheduleBatchReqSt

    public ScheduleBatchReqSt matchTeacher(ScheduleBatchReqSt reqSt){
        return restTemplate.postForObject(matchTeacherUrl,reqSt,ScheduleBatchReqSt.class);
    }
//    public RelationUserDto getRelationUse(String accessToken, Long studentId){
//        return restTemplate.getForObject(authStudentUrl,RelationUserDto.class,studentId,accessToken);
//    }
//    public FishCardDto getFishCard(Long studentId,String orderType){
//        if(Strings.isNullOrEmpty(orderType)){
//            return restTemplate.getForObject(fishcardUrl,FishCardDto.class,studentId);
//        }else
//            return restTemplate.getForObject(fishcardUrl + "?order_type={orderType}",FishCardDto.class,studentId,orderType);
//
//    }
}
