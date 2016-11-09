package com.boxfishedu.workorder.servicex.instantclass;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.requester.InstantTeacherRequester;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.instantclass.grabordervalidator.GrabInstantClassValidators;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by hucl on 16/11/9.
 */
@Component
public class TeacherInstantClassServiceX {
    //TODO:缓存的超时时间
    @Autowired
    private @Qualifier("teachingServiceRedisTemplate") StringRedisTemplate redisTemplate;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private InstantTeacherRequester instantTeacherRequester;

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GrabInstantClassValidators grabInstantClassValidators;


    public JsonResultModel teacherInstantClass(TeacherInstantRequestParam teacherInstantRequestParam) {
        putParameterIntoThreadLocal(teacherInstantRequestParam);
        //校验是否需要走抢单的流程
        TeacherInstantClassStatus validateResult=grabInstantClassValidators.preValidate();
        if(!validateResult.toString().equals(InstantClassRequestStatus.UNKNOWN.toString())){
            return JsonResultModel.newJsonResultModel(InstantClassResult
                    .newInstantClassResult(validateResult));
        }

        //前往师生运营校验
        InstantClassCard instantClassCard=instantClassJpaRepository.findOne(teacherInstantRequestParam.getCardId());
        InstantTeacherRequester.InstantAssignTeacher instantAssignTeacher=null;
        try {
            instantAssignTeacher=instantTeacherRequester.assignGrabteacher(instantClassCard,teacherInstantRequestParam);
        }
        catch (Exception ex){
            return JsonResultModel.newJsonResultModel(InstantClassResult
                    .newInstantClassResult(TeacherInstantClassStatus.FAIL_TO_MATCH));
        }

        //入InstantCard库




        //将数据移到work_order,course_schedule,将数据保存到mongo

        //通知小马生成群组关系

        //将群组关系等数据返回给App







        InstantClassResult instantClassResult = new InstantClassResult();
        long value = new Date().getTime();
        if (value % 3 != 0) {
//            instantClassResult.setGroupId("sasasasasasasasa");
            instantClassResult.setStatus(TeacherInstantClassStatus.FAIL_TO_MATCH.getCode());
            instantClassResult.setDesc(TeacherInstantClassStatus.FAIL_TO_MATCH.getDesc());
        } else {
//            instantClassResult.setGroupId("sasasasasasasasa");
            instantClassResult.setStatus(TeacherInstantClassStatus.MATCHED.getCode());
            instantClassResult.setDesc(TeacherInstantClassStatus.MATCHED.getDesc());
        }
        return JsonResultModel.newJsonResultModel(instantClassResult);
    }

    private void putParameterIntoThreadLocal(TeacherInstantRequestParam teacherInstantRequestParam){
        ThreadLocalUtil.TeacherInstantParamThreadLocal.set(teacherInstantRequestParam);
    }
}
