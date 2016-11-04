package com.boxfishedu.workorder.servicex.instantclass;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * Created by hucl on 16/11/3.
 */
@Component
public class InstantClassServiceX {
    private ValueOperations<String, Long> opsForValue;

    @Autowired
    private @Qualifier("stringLongRedisTemplate") RedisTemplate<String,Long> stringLongRedisTemplate;

    private String generateKey(Long studentId){
        return "InstantClass:user:"+studentId;
    }

    @PostConstruct
    public void initOpsForValue(){
        opsForValue=stringLongRedisTemplate.opsForValue();
    }

    public JsonResultModel instantClass(InstantRequestParam instantRequestParam){
        InstantClassResult instantClassResult=new InstantClassResult();
        if(!opsForValue.setIfAbsent(generateKey(instantRequestParam.getStudentId()),1l)){
            long value=opsForValue.get(generateKey(instantRequestParam.getStudentId()));
            System.out.println(value);
            opsForValue.getAndSet(generateKey(instantRequestParam.getStudentId()),value+1);
            long value2=opsForValue.get(generateKey(instantRequestParam.getStudentId()));
            if(value2%8==0){
                instantClassResult.setStatus(InstantClassRequestStatus.MATCHED.getCode());
                instantClassResult.setDesc(InstantClassRequestStatus.MATCHED.getDesc());
                instantClassResult.setGroupId("1111QQWWWWWWW112222");
            }
            else{
                instantClassResult.setStatus(InstantClassRequestStatus.WAIT_TO_MATCH.getCode());
                instantClassResult.setDesc(InstantClassRequestStatus.WAIT_TO_MATCH.getDesc());
            }
        }
        return JsonResultModel.newJsonResultModel(instantClassResult);
    }

    public JsonResultModel teacherInstantClass(TeacherInstantRequestParam teacherInstantRequestParam) {
        InstantClassResult instantClassResult=new InstantClassResult();
        long value=new Date().getTime();
        if(value%3!=0){
            instantClassResult.setGroupId("sasasasasasasasa");
            instantClassResult.setStatus(TeacherInstantClassStatus.FAIL_TO_MATCH.getCode());
            instantClassResult.setDesc(TeacherInstantClassStatus.FAIL_TO_MATCH.getDesc());
        }
        else{
            instantClassResult.setGroupId("sasasasasasasasa");
            instantClassResult.setStatus(TeacherInstantClassStatus.MATCHED.getCode());
            instantClassResult.setDesc(TeacherInstantClassStatus.MATCHED.getDesc());
        }
        return JsonResultModel.newJsonResultModel(instantClassResult);
    }
}
