package com.boxfishedu.workorder.web.controller.instantclass;

import com.alibaba.fastjson.JSON;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.service.baseTime.BaseTimeSlotService;
import com.boxfishedu.workorder.servicex.instantclass.InstantClassServiceX;
import com.boxfishedu.workorder.servicex.instantclass.TeacherInstantClassServiceX;
import com.boxfishedu.workorder.servicex.instantclass.config.DayRangeBean;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.servicex.instantclass.grabordervalidator.GrabInstatntClassKeyGenerator;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionException;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by hucl on 16/11/3.
 * 即时上课
 */
@CrossOrigin
@RestController
@RequestMapping("/")
public class InstantClassController {

    @Autowired
    private InstantClassServiceX instantClassServiceX;

    @Autowired
    private TeacherInstantClassServiceX teacherInstantClassServiceX;

    @Autowired
    private InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository;


    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")


    StringRedisTemplate redisTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/service/student/instantclass", method = RequestMethod.POST)
    public JsonResultModel instantClass(@RequestBody InstantRequestParam instantRequestParam, Long userId) {
        logger.debug("→_→→_→→_→→_→→_→→_>>>>>>>> IIIIIIIIIIIIIII grabstudent[{}] 学生立即上课请求,参数{}"
                , instantRequestParam.getStudentId(), JacksonUtil.toJSon(instantRequestParam));
        JsonResultModel jsonResultModel = instantClassServiceX.instantClass(instantRequestParam);
        logger.debug("→_→→_→→_→→_→→_→→_<<<<<<<< IIIIIIIIIIIIIII grabstudent[{}] 学生立即上课返回,参数{},结果{}"
                , instantRequestParam.getStudentId(), JacksonUtil.toJSon(instantRequestParam), JacksonUtil.toJSon(jsonResultModel));
        return jsonResultModel;
    }

    @RequestMapping(value = "/service/teacher/instantclass", method = RequestMethod.POST)
    public JsonResultModel teacherInstantClass(@RequestBody TeacherInstantRequestParam teacherInstantRequestParam, Long userId) {
        logger.info("x__xx__xx__xx__x>>>> IIIIIIIIIIIIIII 教师立即上课抢单,参数{}", JacksonUtil.toJSon(teacherInstantRequestParam));
        try {
            JsonResultModel jsonResultModel = teacherInstantClassServiceX.teacherInstantClass(teacherInstantRequestParam);
            return jsonResultModel;
        } catch (Exception ex) {
            JsonResultModel jsonResultModel = JsonResultModel.newJsonResultModel(InstantClassResult
                    .newInstantClassResult(TeacherInstantClassStatus.FAIL_TO_MATCH));
            logger.error("/(ㄒoㄒ)/~~/(ㄒoㄒ)/~~/(ㄒoㄒ)/~~ IIIIIIIIIIIIIII  grabresult ,instantcard:[{}],teacher:[{}]/(ㄒoㄒ)/~~/(ㄒoㄒ)/~~失败,结果:{}"
                    , teacherInstantRequestParam.getCardId(), teacherInstantRequestParam.getTeacherId(), JacksonUtil.toJSon(jsonResultModel), ex);
            return jsonResultModel;
        } finally {
            String key = GrabInstatntClassKeyGenerator.generateKey(teacherInstantRequestParam);
            //无论是否成功都删除当前用户的资源,需要增加判断
            String lockedUserId = redisTemplate.opsForValue().get(key);
            if (lockedUserId != null) {
                if (lockedUserId.equals(teacherInstantRequestParam.getTeacherId().toString())) {
                    logger.debug("@teacherInstantClass IIIIIIIIIIIIIII 参数为[{}]的抢单教师[{}]结束抢单,删除redis数据,正在退出..."
                            , JacksonUtil.toJSon(teacherInstantRequestParam), lockedUserId);
                    redisTemplate.delete(key);
                } else {
                    logger.debug("@teacherInstantClass IIIIIIIIIIIIIII (ㄒoㄒ)/~~/(ㄒoㄒ) 参数为[{}]的抢单教师[{}]完成抢单,与正在抢单的教师[{}]不是同一个教师,正在退出..."
                            , JacksonUtil.toJSon(teacherInstantRequestParam), teacherInstantRequestParam.getTeacherId(), lockedUserId);
                }
            }
        }
    }

    /**
     * 获取学生端的上课时间情况
     *
     * @return
     */
    @RequestMapping(value = "/service/student/instant/timerange", method = RequestMethod.GET)
    public JsonResultModel timeRange(Long userId) {
        String timeDesc = instantClassServiceX.timeRange(userId);
        if (StringUtils.isEmpty(timeDesc)) {
            return JsonResultModel.newJsonResultModel(null);
        }
        return JsonResultModel.newJsonResultModel(timeDesc + " 开启");
    }

    /**
     * @param date "2016-11-12"
     * @return
     */
    @RequestMapping(value = "/instanttimes/date/{date}", method = RequestMethod.GET)
    public JsonResultModel getRangeByDay(@PathVariable("date") String date) {
        Optional<List<InstantClassTimeRules>> instantClassTimeRulesList = instantClassTimeRulesMorphiaRepository
                .getByDay(date, TutorTypeEnum.FRN.toString());
        return JsonResultModel.newJsonResultModel(instantClassServiceX.getSortedTimeRulesList(instantClassTimeRulesList.get()));
    }

    /**
     * 教师端显示时间片用
     */
    @RequestMapping(value = "/service/teacher/instant/range", method = RequestMethod.GET)
    public JsonResultModel getTeacherRangeByDay(Long userId) {
        return instantClassServiceX.getTeacherRangeByDay(userId);
    }

    //初始化数据
    //@RequestMapping(value = "/instanttimes/date", method = RequestMethod.POST)
    @RequestMapping(value = "/instanttimes/initdata/date", method = RequestMethod.POST)
    public JsonResultModel instantDayClassTimes(@RequestBody DayRangeBean dateInfo) {
        instantClassServiceX.initTimeRange(dateInfo);
        return JsonResultModel.newJsonResultModel("ok");
    }

    /**
     * 判断学生当前的课程表中有中教,外教,还是都有
     *
     * @param studentId
     * @return
     */
    @RequestMapping(value = "/service/student/scheduletype/{student_id}", method = RequestMethod.GET)
    public JsonResultModel getScheduleType(@PathVariable("student_id") Long studentId) {
        return instantClassServiceX.getScheduleType(studentId);
    }

}
