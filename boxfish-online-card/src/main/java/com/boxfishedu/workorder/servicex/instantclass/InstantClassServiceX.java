package com.boxfishedu.workorder.servicex.instantclass;

import ch.qos.logback.core.joran.conditional.ElseAction;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.servicex.instantclass.instantvalidator.InstantClassValidators;
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
    private InstantClassValidators instantClassValidators;

    @Autowired
    private
    @Qualifier("stringLongRedisTemplate")
    RedisTemplate<String, Long> stringLongRedisTemplate;

    private String generateKey(Long studentId) {
        return "InstantClass:user:" + studentId;
    }

    @PostConstruct
    public void initOpsForValue() {
        opsForValue = stringLongRedisTemplate.opsForValue();
    }

    public JsonResultModel instantClass(InstantRequestParam instantRequestParam) {
        //对用户当前行为进行校验
        int validateResult=instantClassValidators.preValidate(instantRequestParam);
        if(validateResult>InstantClassRequestStatus.UNKNOWN.getCode()){
            return JsonResultModel.newJsonResultModel(new InstantClassResult(InstantClassRequestStatus.getEnumByCode(validateResult)));
        }

        long visitCount = opsForValue.increment(generateKey(instantRequestParam.getStudentId()), 1l);
        if (visitCount % 8 == 0) {
            return JsonResultModel.newJsonResultModel(new InstantClassResult(InstantClassRequestStatus.MATCHED, "11111221221212QQWW"));
        } else if (visitCount % 33 == 0) {
            return JsonResultModel.newJsonResultModel(new InstantClassResult(InstantClassRequestStatus.ASK_TOO_BUSY));
        } else {
            return JsonResultModel.newJsonResultModel(new InstantClassResult(InstantClassRequestStatus.NO_MATCH));
        }
    }

    public JsonResultModel teacherInstantClass(TeacherInstantRequestParam teacherInstantRequestParam) {
        InstantClassResult instantClassResult = new InstantClassResult();
        long value = new Date().getTime();
        if (value % 3 != 0) {
            instantClassResult.setGroupId("sasasasasasasasa");
            instantClassResult.setStatus(TeacherInstantClassStatus.FAIL_TO_MATCH.getCode());
            instantClassResult.setDesc(TeacherInstantClassStatus.FAIL_TO_MATCH.getDesc());
        } else {
            instantClassResult.setGroupId("sasasasasasasasa");
            instantClassResult.setStatus(TeacherInstantClassStatus.MATCHED.getCode());
            instantClassResult.setDesc(TeacherInstantClassStatus.MATCHED.getDesc());
        }
        return JsonResultModel.newJsonResultModel(instantClassResult);
    }
}
