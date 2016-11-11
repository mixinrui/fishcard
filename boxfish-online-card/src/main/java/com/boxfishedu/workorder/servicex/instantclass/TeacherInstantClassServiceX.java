package com.boxfishedu.workorder.servicex.instantclass;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.InstantTeacherRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.instantclass.InstantClassTeacherService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.instantclass.grabordervalidator.GrabInstantClassValidators;
import com.boxfishedu.workorder.servicex.instantclass.grabordervalidator.GrabInstatntClassKeyGenerator;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.result.InstantGroupInfo;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private InstantClassTeacherService instantClassTeacherService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private WorkOrderService workOrderService;


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

        //入InstantCard库,初始化鱼卡信息,创建群组
        InstantGroupInfo instantGroupInfo =instantClassTeacherService
                .prepareForInstantClass(instantClassCard,instantAssignTeacher);

        //将该鱼卡标记为已匹配教师;时间为1天
        this.markMatchedIntoRedis(teacherInstantRequestParam);

        //创建群组,将群组数据返回给App
        return JsonResultModel.newJsonResultModel(InstantClassResult
                .newInstantClassResult(updateGroupInfoInstantCard(instantGroupInfo,instantClassCard),TeacherInstantClassStatus.MATCHED));
    }

    private void markMatchedIntoRedis(TeacherInstantRequestParam teacherInstantRequestParam){
        String matchedKey=GrabInstatntClassKeyGenerator.matchedKey(teacherInstantRequestParam.getCardId());
        redisTemplate.opsForValue().setIfAbsent(matchedKey, teacherInstantRequestParam.getTeacherId().toString());
        redisTemplate.expire(matchedKey,1, TimeUnit.DAYS);
    }

    private void putParameterIntoThreadLocal(TeacherInstantRequestParam teacherInstantRequestParam){
        ThreadLocalUtil.TeacherInstantParamThreadLocal.set(teacherInstantRequestParam);
    }

    private InstantClassCard updateGroupInfoInstantCard(InstantGroupInfo instantGroupInfo,InstantClassCard instantClassCard){
        instantClassCard.setGroupName(instantGroupInfo.getGroupName());
        instantClassCard.setGroupId(instantGroupInfo.getGroupId());
        instantClassCard.setChatRoomId(instantGroupInfo.getChatRoomId());
        return instantClassJpaRepository.save(instantClassCard);
    }
}
