package com.boxfishedu.workorder.servicex.instantclass.tutorrange;

import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.instantclass.bean.TeacherInstantRangeBean;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/12/20.
 * template
 */
public interface ITutorRangeStrategy {
    Logger logger = LoggerFactory.getLogger("ITutorRangeStrategy");

    /**
     * 学生立即上课返回的值
     *
     * @param redisTemplate
     * @return
     */
    default String studentTimeRange(StringRedisTemplate redisTemplate) {
        String timeDesc;
        try {
            timeDesc = redisTemplate.opsForValue().get(this.timeRangeKey());
            if (StringUtils.isNotEmpty(timeDesc)) {
                return timeDesc;
            }
        } catch (Exception ex) {
            logger.error("从redis获取当天可上课时间片失败，从mongo获取");
        }

        Optional<List<InstantClassTimeRules>> instantClassTimeRulesList = this.getInstantTimeList();

        if (!instantClassTimeRulesList.isPresent()) {
            return StringUtils.EMPTY;
        }

        timeDesc = this.timeDesc(this.getSortedTimeRulesList(instantClassTimeRulesList.get()));

        this.putTimeRange(redisTemplate, timeDesc);

        return timeDesc;
    }

    //把描述放入redis
    default void putTimeRange(StringRedisTemplate stringRedisTemplate, String desc) {
        stringRedisTemplate.opsForValue().setIfAbsent(this.timeRangeKey(), desc);
    }

    default String timeDesc(List<InstantClassTimeRules> rawTimeRules) {
        List<String> timeStringRules = rawTimeRules.stream()
                .map(timeLimitRule -> String.join("-", timeLimitRule.getBegin().substring(0, 5), timeLimitRule.getEnd().substring(0, 5)))
                .collect(Collectors.toList());
        return StringUtils.join(timeStringRules, " & ");
    }

    default List<InstantClassTimeRules> getSortedTimeRulesList(List<InstantClassTimeRules> rawTimeRules) {
        return rawTimeRules.stream().sorted(Comparator.comparing(instantClassTimeRules ->
                DateUtil.String2Date(String.join(" ", DateUtil.date2SimpleString(new Date()), instantClassTimeRules.getBegin())).getTime()
        )).collect(Collectors.toList());
    }

    String timeRangeKey();

    Optional<List<InstantClassTimeRules>> getInstantTimeList();


    /**
     * 教师相关操作
     *
     * @param teacherStudentRequester
     * @param teacherId
     * @return
     */
    default TeacherInstantRangeBean teacherTimeRange(TeacherStudentRequester teacherStudentRequester
            , InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository, Long teacherId) {
        TutorTypeEnum tutorTypeEnum = this.getTeacherTutorType(teacherStudentRequester, teacherId);

        Optional<List<InstantClassTimeRules>> instantClassTimeRulesList
                = this.getTeacherInstantTimeList(instantClassTimeRulesMorphiaRepository, tutorTypeEnum);

        if (!instantClassTimeRulesList.isPresent()|| CollectionUtils.isEmpty(instantClassTimeRulesList.get())) {
            return TeacherInstantRangeBean.defaultRange();
        }

        List<InstantClassTimeRules> instantClassTimeRules = this.getSortedTimeRulesList(instantClassTimeRulesList.get());
        return TeacherInstantRangeBean.getInstantRange(instantClassTimeRules);
    }

    default TutorTypeEnum getTeacherTutorType(TeacherStudentRequester teacherStudentRequester, Long teacherId) {
        int teacherType = teacherStudentRequester.getTeacherType(teacherId);
        return ((TeachingType) TeachingType.get(teacherType)).teachingType2TutorType();
    }


    default Optional<List<InstantClassTimeRules>> getTeacherInstantTimeList(InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository
            , TutorTypeEnum tutorTypeEnum) {
        Optional<List<InstantClassTimeRules>> instantClassTimeRulesList = instantClassTimeRulesMorphiaRepository
                .getByDay(DateUtil.date2SimpleString(new Date()), tutorTypeEnum.toString());

        return instantClassTimeRulesList;
    }
}
