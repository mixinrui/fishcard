package com.boxfishedu.workorder.service;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.entity.mysql.BaseTimeSlots;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Created by jiaozijun on 16/11/24.
 */
@Component
public class RedisMapService {


    @Autowired
    private @Qualifier("teachingServiceRedisTemplate")
    StringRedisTemplate redisTemplate;

    public void setMap(String key ,String date , List<BaseTimeSlots> baseTimeSlotsList){
        String key_Front= CacheKeyConstant.BASE_TIME_SLOTS;
        key = key_Front+key;
        redisTemplate.opsForHash().putIfAbsent(key,date, JSONObject.toJSONString(baseTimeSlotsList ) );
    }

    public List<BaseTimeSlots> getMap(String key,String date){
        String key_Front= CacheKeyConstant.BASE_TIME_SLOTS;
        key = key_Front+key;
        Object text = redisTemplate.opsForHash().get(key,date);
        return    JSONObject.parseArray(null==text?null:text.toString() , BaseTimeSlots.class) ;
    }
}
