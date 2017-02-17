package com.boxfishedu.workorder.servicex.callbacklog;

import com.boxfishedu.workorder.common.util.RedisKeyGenerator;
import com.boxfishedu.workorder.web.param.callbacklog.CallBackHeartBeatParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * Created by hucl on 17/2/16.
 */
@Service
public class CallBackLogServiceX {
    private RedisTemplate<String, String> redisTemplate;

    private ZSetOperations<String, String> zSetOperations;

    @Autowired
    public void initRedis(@Qualifier(value = "stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        zSetOperations = redisTemplate.opsForZSet();
    }

    public Long getCardId(CallBackHeartBeatParam callBackHeartBeatParam) {
        return Long.parseLong(
                callBackHeartBeatParam.getMsgBody().get(0).getMsgContent().getData().get("workOrderId").toString());
    }

    public void updateSet(CallBackHeartBeatParam callBackHeartBeatParam) {
        Long cardId = this.getCardId(callBackHeartBeatParam);
        String useMd5 = callBackHeartBeatParam.getFrom_Account().toString();
        String key = RedisKeyGenerator.getGroupClassHeartBeatKey(cardId);
        //暂时只针对
        zSetOperations.add(key, useMd5, new Date().getTime());
    }
}
