package com.boxfishedu.workorder.servicex.instantclass;

import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.InstantTeacherRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.instantclass.InstantClassTeacherService;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.instantclass.grabordervalidator.GrabInstantClassValidators;
import com.boxfishedu.workorder.servicex.instantclass.grabordervalidator.GrabInstatntClassKeyGenerator;
import com.boxfishedu.workorder.servicex.studentrelated.AssignTeacherFixService;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.result.InstantGroupInfo;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by hucl on 16/11/9.
 */
@Component
public class TeacherInstantClassServiceX {
    //TODO:缓存的超时时间
    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private InstantTeacherRequester instantTeacherRequester;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GrabInstantClassValidators grabInstantClassValidators;

    @Autowired
    private InstantClassTeacherService instantClassTeacherService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private AssignTeacherFixService assignTeacherFixService;


    public JsonResultModel teacherInstantClass(TeacherInstantRequestParam teacherInstantRequestParam) {
        putParameterIntoThreadLocal(teacherInstantRequestParam);
        //校验是否需要走抢单的流程
        TeacherInstantClassStatus validateResult = grabInstantClassValidators.preValidate();
        if (validateResult.getCode() != TeacherInstantClassStatus.UNKNOWN.getCode()) {
            return invalidateFailResult(teacherInstantRequestParam, validateResult);
        }

        //前往师生运营校验
        InstantClassCard instantClassCard = instantClassJpaRepository
                .findOne(teacherInstantRequestParam.getCardId());

        InstantTeacherRequester.InstantAssignTeacher instantAssignTeacher = null;
        try {
            instantAssignTeacher = instantTeacherRequester
                    .assignGrabteacher(instantClassCard, teacherInstantRequestParam);
        } catch (Exception ex) {
            return this.returnMatchFailResult(teacherInstantRequestParam, ex);
        }

        //入InstantCard库,初始化鱼卡信息,创建群组
        InstantGroupInfo instantGroupInfo = new InstantGroupInfo();
        //获取订单对应的鱼卡
        List<WorkOrder> workOrders = instantClassTeacherService
                .prepareForInstantClass(instantClassCard, instantAssignTeacher, instantGroupInfo);

        //将该鱼卡标记为已匹配教师;时间为1天
        this.markMatchedIntoRedis(teacherInstantRequestParam);
        //异步调用,如果类型发生标变化,教师不能上该节课则变化
        regenerateGroupInfo(workOrders, instantClassCard);

        //更新首页信息
        dataCollectorService.updateBothChnAndFnItemAsync(instantClassCard.getStudentId());

        //创建群组,将群组数据返回给App
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(InstantClassResult
                .newInstantClassResult(updateGroupInfoInstantCard(instantGroupInfo
                        , instantClassCard), TeacherInstantClassStatus.MATCHED));
        //异步操作  // 设置指定老师申请失效
        assignTeacherFixService.disableAssignWorkOrderOut(instantClassCard.getWorkorderId(),"立即上课");

        logger.info("~^o^~~^o^~~^o^~~^o^, IIIIIIIIIIIIIII grabresult ,instantcard:[{}],teacher:[{}]成功,结果:{}"
                , teacherInstantRequestParam.getCardId(), teacherInstantRequestParam.getTeacherId()
                , JacksonUtil.toJSon(jsonResultModel));

        return jsonResultModel;
    }

    //如果当前教师不能上这些课程,将会发生教师更换
    public void regenerateGroupInfo(List<WorkOrder> workOrders, InstantClassCard instantClassCard) {
        //其他入口,直接返回
        if (1 == instantClassCard.getEntrance()) {
            return;
        }
        if (CollectionUtils.isEmpty(workOrders)) {
            return;
        }
        threadPoolManager.execute(new Thread(() -> {
            for (int i = 0; i < workOrders.size(); i++) {
                Boolean result = workOrderService.changeTeacherForTypeChanged(workOrders.get(i));
                //如果能上此种类型的课程,重新生成关系
                if (BooleanUtils.isFalse(result)) {
                    courseOnlineRequester.rebuildGroup(Arrays.asList(workOrders.get(i)));
                }
            }
        }));
    }

    private void markMatchedIntoRedis(TeacherInstantRequestParam teacherInstantRequestParam) {
        try {
            String matchedKey = GrabInstatntClassKeyGenerator.matchedKey(teacherInstantRequestParam.getCardId());
            if (redisTemplate.opsForValue().setIfAbsent(matchedKey, teacherInstantRequestParam.getTeacherId().toString())) {
                logger.debug("@markMatchedIntoRedis#参数[{}],往redis中存入匹配上的情况成功"
                        , JacksonUtil.toJSon(teacherInstantRequestParam));
            } else {
                logger.error("@markMatchedIntoRedis#参数[{}],往redis中存入匹配上的情况失败,该课已被教师[{}]匹配走"
                        , JacksonUtil.toJSon(teacherInstantRequestParam), redisTemplate.opsForValue().get(matchedKey));
            }
            redisTemplate.expire(matchedKey, 1, TimeUnit.DAYS);
        } catch (Exception ex) {
            logger.error("@markMatchedIntoRedis#将匹配结果保存入redis报错", ex);
        }
    }

    private void putParameterIntoThreadLocal(TeacherInstantRequestParam teacherInstantRequestParam) {
        ThreadLocalUtil.TeacherInstantParamThreadLocal.set(teacherInstantRequestParam);
    }

    private InstantClassCard updateGroupInfoInstantCard(InstantGroupInfo instantGroupInfo
            , InstantClassCard instantClassCard) {

        instantClassCard.setGroupName(instantGroupInfo.getGroupName());
        instantClassCard.setGroupId(instantGroupInfo.getGroupId());
        instantClassCard.setChatRoomId(instantGroupInfo.getChatRoomId());

        return instantClassJpaRepository.save(instantClassCard);
    }

    private JsonResultModel returnMatchFailResult(TeacherInstantRequestParam teacherInstantRequestParam, Exception ex) {
        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(InstantClassResult
                .newInstantClassResult(TeacherInstantClassStatus.FAIL_TO_MATCH));

        logger.error("/(ㄒoㄒ)/~~/(ㄒoㄒ)/~~/(ㄒoㄒ)/~~ IIIIIIIIIIIIIII  grabresult fail,师生运营校验不通过,instantcard:[{}]" +
                        ",teacher:[{}]~~失败,结果:{}", teacherInstantRequestParam.getCardId()
                , teacherInstantRequestParam.getTeacherId(), JacksonUtil.toJSon(jsonResultModel), ex);

        return jsonResultModel;
    }


    private JsonResultModel invalidateFailResult(TeacherInstantRequestParam teacherInstantRequestParam
            , TeacherInstantClassStatus validateResult) {

        JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(InstantClassResult
                .newInstantClassResult(validateResult));

        logger.error("/(ㄒoㄒ)/~~/(ㄒoㄒ)/~~/(ㄒoㄒ)/~~ IIIIIIIIIIIIIII grabresult ,校验不通过,instantcard:[{}],teacher:[{}]~~失败,结果:{}"
                , teacherInstantRequestParam.getCardId(), teacherInstantRequestParam.getTeacherId(), JacksonUtil.toJSon(jsonResultModel));

        return jsonResultModel;
    }
}
