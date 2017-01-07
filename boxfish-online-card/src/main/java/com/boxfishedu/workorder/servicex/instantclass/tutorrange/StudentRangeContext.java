package com.boxfishedu.workorder.servicex.instantclass.tutorrange;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.service.instantclass.InstantClassService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * Created by hucl on 16/12/21.
 */
@Component
public class StudentRangeContext {
    @Autowired
    private java.util.Map<String, ITutorRangeStrategy> tutorRangeStrategyMap;

    @Autowired
    private
    @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    @Autowired
    private InstantClassService instantClassService;

    //给前端展示时使用
    public String studentTimeRange(Long studentId) {
        Map<String, Object> map = instantClassService.getScheduleTypeMap(studentId);
        if (CollectionUtils.isEmpty(map)) {
            //默认使用外教
            return tutorRangeStrategyMap.get(InstantRangeEnum.FRN_RANGE).studentTimeRange(redisTemplate);
        }
        String status = map.get("status").toString();
        if (StringUtils.isEmpty(status)) {
            return tutorRangeStrategyMap.get(InstantRangeEnum.FRN_RANGE).studentTimeRange(redisTemplate);
        }

        if (TeachingType.ZHONGJIAO.getCode() == Integer.parseInt(status)) {
            return tutorRangeStrategyMap.get(InstantRangeEnum.CN_RANGE).studentTimeRange(redisTemplate);
        } else {
            return tutorRangeStrategyMap.get(InstantRangeEnum.FRN_RANGE).studentTimeRange(redisTemplate);
        }
    }

    //给学生提示时候使用
    public String studentTimeRange(TutorTypeEnum tutorTypeEnum) {
        String rangeEnum = InstantRangeEnum.tutorType2Range(tutorTypeEnum);
        return tutorRangeStrategyMap.get(rangeEnum).studentTimeRange(redisTemplate);
    }

    public String timeRangeKey(TutorTypeEnum tutorTypeEnum) {
        String rangeEnum = InstantRangeEnum.tutorType2Range(tutorTypeEnum);
        return tutorRangeStrategyMap.get(rangeEnum).timeRangeKey();
    }
}
