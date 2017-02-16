package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.util.RedisKeyGenerator;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.mongo.ConfigBeanMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.callbacklog.CallBackLogServiceX;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by hucl on 17/1/5.
 */
@Service
public class SmallClassServiceX {

    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

    RedisTemplate<String, String> redisTemplate;

    ZSetOperations<String, String> zSetOperations;

    @Autowired
    CallBackLogServiceX callBackLogServiceX;

    @Autowired
    ConfigBeanMorphiaRepository configBeanMorphiaRepository;

    @Autowired
    public void initRedis(@Qualifier(value = "stringLongRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOperations = redisTemplate.opsForZSet();
    }

    public Map<String, Object> getTeacherValidateMap(Long smallClassId) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        //10:too early   20:completed   30:success

        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        LocalDateTime startTime = LocalDateTime.ofInstant(smallClass.getStartTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime deadTime = startTime.plusMinutes(30);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        //TODO:测试
//        map.put("status", 30);
//        map.put("statusDesc", "success");
//        map.put("classInfo", smallClass);

        if (now.isBefore(startTime)) {
            map.put("status", 10);
            map.put("statusDesc", "too early");
            map.put("classInfo", null);
        } else if (now.isAfter(deadTime)) {
            map.put("status", 20);
            map.put("statusDesc", "completed");
            map.put("classInfo", null);
        } else {
            map.put("status", 30);
            map.put("statusDesc", "success");
            map.put("classInfo", smallClass);
        }
        return map;
    }

    // 验证小班课老师是否处于大于1000的状态  true  给出提示  false  不给提示
    public boolean checkChangeTeacherForSmallClass(Long smallClassId) {

        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        if (null != smallClass && smallClass.getStatus() >= PublicClassInfoConstantStatus.TEACHER_CARD_VALIDATED) {
            return true;
        }
        return false;
    }

    public Set<String> selectRandomStudentList(Long smallClassId) {
        String key = RedisKeyGenerator.getGroupClassHeartBeatKey(smallClassId);

        Long limit = configBeanMorphiaRepository.getSingleBean().getSelectStudentNum();
        if (Objects.isNull(limit)) {
            limit = 1l;
        }

        Set<String> students = zSetOperations.range(key, 0, limit);

        return students;
    }
}
