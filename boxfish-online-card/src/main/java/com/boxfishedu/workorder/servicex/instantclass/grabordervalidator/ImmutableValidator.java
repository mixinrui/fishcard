package com.boxfishedu.workorder.servicex.instantclass.grabordervalidator;

import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created by hucl on 16/11/4.
 */
@Order(0)
@Component
public class ImmutableValidator implements IGrabInstantClassValidator {
    @Autowired
    private @Qualifier("teachingServiceRedisTemplate") StringRedisTemplate redisTemplate;

    private ValueOperations<String,String> opsValue;

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    void init(){
        opsValue=redisTemplate.opsForValue();
    }

    @Override
    public TeacherInstantClassStatus preValidate() {
        logger.debug(">>>>>0:ImmutableValidator;redis校验");
        String key=GrabInstatntClassKeyGenerator.generateKey(ThreadLocalUtil.getTeacherInstantParam());
        TeacherInstantRequestParam teacherInstantRequestParam=ThreadLocalUtil.getTeacherInstantParam();
        String matchedResult=opsValue.get(GrabInstatntClassKeyGenerator.matchedKey(teacherInstantRequestParam.getCardId()));
        if(!StringUtils.isEmpty(matchedResult)){
            logger.debug("课程已经被教师[{}]抢走;退出抢单......",matchedResult);
            return TeacherInstantClassStatus.FAIL_TO_MATCH;
        }
        //如果失败了
        if(!opsValue.setIfAbsent(key,teacherInstantRequestParam.getTeacherId().toString())){
            logger.debug("teacherInstantClass#fail#card:{}#教师:{},已经有教师正在抢单,退出抢单...."
                    ,teacherInstantRequestParam.getCardId(),teacherInstantRequestParam.getTeacherId());
            return TeacherInstantClassStatus.FAIL_TO_MATCH;
        }
        else{
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
            return TeacherInstantClassStatus.UNKNOWN;
        }
    }
}