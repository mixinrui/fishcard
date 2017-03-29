package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.util.RedisKeyGenerator;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.mongo.ConfigBeanMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.smallclass.SelectStudentsService;
import com.boxfishedu.workorder.servicex.callbacklog.CallBackLogServiceX;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    SetOperations<String, String> setOperations;

    ListOperations<String, String> listOperations;

    @Autowired
    CallBackLogServiceX callBackLogServiceX;

    @Autowired
    ConfigBeanMorphiaRepository configBeanMorphiaRepository;

    @Autowired
    SelectStudentsService selectStudentsService;

    @Autowired
    public void initRedis(@Qualifier(value = "stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOperations = redisTemplate.opsForZSet();
        this.setOperations = redisTemplate.opsForSet();
        this.listOperations = redisTemplate.opsForList();
    }

    public Map<String, Object> getTeacherValidateMap(Long smallClassId) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        //10:too early   20:completed   30:success

        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        LocalDateTime startTime = LocalDateTime.ofInstant(smallClass.getStartTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime deadTime = startTime.plusMinutes(30);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

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


    // 验证小班课老师是否123处于大于1000的状态  true  给出提示  false  不给提示
    public boolean checkChangeTeacherForSmallClass(Long smallClassId) {

        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);
        if (null != smallClass && smallClass.getStatus() >= PublicClassInfoConstantStatus.TEACHER_CARD_VALIDATED) {
            return true;
        }
        return false;
    }

    public String selectCandidate(Long smallClassId) {
        return selectStudentsService.selectCandidate(smallClassId);
    }

    public Long candidateInterval() {
        String interval = configBeanMorphiaRepository.getSingleBean().getCandidateInterval();
        if (Objects.isNull(interval)) {
            return 3l;
        }
        return Long.parseLong(interval);
    }

    public Long currentPageIndex(Long smallClassId) {
        String key = RedisKeyGenerator.getTeacherOperationKey(smallClassId);
        List<String> pageIndexes = listOperations.range(key, 0, 0);
        if (CollectionUtils.isEmpty(pageIndexes)) {
            return -1l;
        }
        return Long.parseLong(pageIndexes.get(0));
    }
}
