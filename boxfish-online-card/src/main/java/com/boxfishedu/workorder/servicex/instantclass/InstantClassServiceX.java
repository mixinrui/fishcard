package com.boxfishedu.workorder.servicex.instantclass;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.joran.conditional.ElseAction;
import com.alibaba.fastjson.JSON;
import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.entity.mongo.TimeLimitRules;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.instantclass.InstantClassService;
import com.boxfishedu.workorder.servicex.instantclass.bean.TeacherInstantRangeBean;
import com.boxfishedu.workorder.servicex.instantclass.config.DayRangeBean;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.instantclass.instantvalidator.InstantClassValidators;
import com.boxfishedu.workorder.servicex.instantclass.tutorrange.StudentRangeContext;
import com.boxfishedu.workorder.servicex.instantclass.tutorrange.TeacherRangeContext;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/11/3.
 */
@Component
public class InstantClassServiceX {
    private ValueOperations<String, Long> opsForValue;

    @Autowired
    private InstantClassValidators instantClassValidators;

    @Autowired
    private InstantClassService instantClassService;

    @Autowired
    private InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository;

    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private
    @Qualifier("stringLongRedisTemplate")
    RedisTemplate<String, Long> stringLongRedisTemplate;

    @Autowired
    private TeacherRangeContext teacherRangeContext;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    @Autowired
    private StudentRangeContext studentRangeContext;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private String generateKey(Long studentId) {
        return "InstantClass:user:" + studentId;
    }


    @PostConstruct
    public void initOpsForValue() {
        opsForValue = stringLongRedisTemplate.opsForValue();
    }

    public JsonResultModel instantClass(InstantRequestParam instantRequestParam) {
        putParameterIntoThreadLocal(instantRequestParam);

        //对用户当前行为进行校验
        int validateResult = instantClassValidators.preValidate();
        if (validateResult > InstantClassRequestStatus.UNKNOWN.getCode()) {
            InstantClassRequestStatus instantStatus = InstantClassRequestStatus.getEnumByCode(validateResult);
            return this.invalidReturn(instantStatus, instantRequestParam);
        }
        return JsonResultModel.newJsonResultModel(instantClassService.getMatchResult());
    }

    //给用户界面展示使用
    public String timeRange(Long studentId) {
        return studentRangeContext.studentTimeRange(studentId);
    }

    public String timeRange(TutorTypeEnum tutorTypeEnum) {
        return studentRangeContext.studentTimeRange(tutorTypeEnum);
    }

    public List<InstantClassTimeRules> getSortedTimeRulesList(List<InstantClassTimeRules> rawTimeRules) {
        return rawTimeRules.stream().sorted(Comparator.comparing(instantClassTimeRules ->
                DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(new Date()), instantClassTimeRules.getBegin())).getTime()
        )).collect(Collectors.toList());
    }

    public JsonResultModel getTeacherRangeByDay(Long userId) {
        return JsonResultModel.newJsonResultModel(teacherRangeContext
                .teacherTimeRange(teacherStudentRequester, instantClassTimeRulesMorphiaRepository, userId));
    }

    public JsonResultModel getScheduleType(Long studentId) {
        return JsonResultModel.newJsonResultModel(instantClassService.getScheduleTypeMap(studentId));
    }

    private JsonResultModel invalidReturn(InstantClassRequestStatus instantStatus, InstantRequestParam instantRequestParam) {
        switch (instantStatus) {
            case NOT_IN_RANGE:
                return JsonResultModel.newJsonResultModel(InstantClassResult
                        .newInstantClassResult(instantStatus, String.format("实时上课会在[%s]内开启,请到时间再重试~"
                                , this.timeRange(TutorTypeEnum.getByValue(instantRequestParam.getTutorType())))));

            case HAVE_CLASS_IN_HALF_HOURS:
                String msg = String.format("您预约了[%s]的课程,马上就开始了,此时不能实时上课~"
                        , DateUtil.dateTrimYear(ThreadLocalUtil.classDateIn30Minutes.get()));

                return JsonResultModel.newJsonResultModel(InstantClassResult
                        .newInstantClassResult(instantStatus, msg));

            case MATCHED_LESS_THAN_30MINUTES: {
                JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(InstantClassResult
                        .newInstantClassResult(ThreadLocalUtil.instantCardMatched30Minutes.get(), InstantClassRequestStatus.MATCHED));
                if (ThreadLocalUtil.instantCardMatched30Minutes.get().getMatchResultReadFlag() != 1) {
                    instantClassJpaRepository.updateMatchedReadFlag(ThreadLocalUtil.instantCardMatched30Minutes.get().getId(), 1);
                }
                return jsonResultModel;
            }

            case UNFINISHED_COURSE: {
                return JsonResultModel.newJsonResultModel(InstantClassResult
                        .newInstantClassResult(instantStatus, ThreadLocalUtil.unFinishedCourses30MinutesTips.get()));
            }

            default:
                return JsonResultModel.newJsonResultModel(InstantClassResult
                        .newInstantClassResult(instantStatus));
        }
    }

    private void putParameterIntoThreadLocal(InstantRequestParam instantRequestParam) {
        if (StringUtils.isEmpty(instantRequestParam.getTutorType())) {
            instantRequestParam.setTutorType(TutorType.FRN.toString());
        }
        ThreadLocalUtil.instantRequestParamThreadLocal.set(instantRequestParam);
    }

    public void initTimeRange(DayRangeBean dateInfo) {
        Date date = DateUtil.String2Date(String.join(" ", dateInfo.getDate(), "00:00:00"));
        if (CollectionUtils.isEmpty(dateInfo.getRange())) {
            throw new BusinessException("参数不合法");
        }
        Optional<List<InstantClassTimeRules>> listOldOptional
                = instantClassTimeRulesMorphiaRepository.getByDay(dateInfo.getDate(), dateInfo.getTutorType());

        //新增新规则
        dateInfo.getRange().forEach(range -> {
            LocalDateTime dateLocal = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            InstantClassTimeRules instantClassTimeRules = new InstantClassTimeRules();
            instantClassTimeRules.setDate(DateUtil.localDate2SimpleString(dateLocal));
            instantClassTimeRules.setTutorType(dateInfo.getTutorType());
            instantClassTimeRules.setDay(dateLocal.getDayOfWeek().toString());
            instantClassTimeRules.setBegin(range.getBegin());
            instantClassTimeRules.setEnd(range.getEnd());
            instantClassTimeRulesMorphiaRepository.save(instantClassTimeRules);
        });

        //删除旧的规则
        listOldOptional.get().forEach(oldRange -> instantClassTimeRulesMorphiaRepository.delete(oldRange));

        //清空缓存
        TutorTypeEnum tutorTypeEnum = TutorTypeEnum.getByValue(dateInfo.getTutorType());
        redisTemplate.delete(studentRangeContext.timeRangeKey(tutorTypeEnum));
    }
}
