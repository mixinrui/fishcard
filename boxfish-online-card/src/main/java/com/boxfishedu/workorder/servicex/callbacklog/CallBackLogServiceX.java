package com.boxfishedu.workorder.servicex.callbacklog;

import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.common.util.RedisKeyGenerator;
import com.boxfishedu.workorder.web.param.callbacklog.CallBackHeartBeatParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        Map<String, Object> map = JacksonUtil.readValue(callBackHeartBeatParam.getMsgBody().get(0).getMsgContent().getData(), HashMap.class);
        return Long.parseLong(
                map.get("workOrderId").toString());
    }

    private String getRole(CallBackHeartBeatParam callBackHeartBeatParam) {
        Map<String, Object> map = JacksonUtil.readValue(callBackHeartBeatParam.getMsgBody().get(0).getMsgContent().getData(), HashMap.class);
        if (Objects.isNull(map.get("role"))) {
            return null;
        }
        return map.get("role").toString();
    }

    public void updateSet(CallBackHeartBeatParam callBackHeartBeatParam) {
        Long cardId = this.getCardId(callBackHeartBeatParam);
        String role = this.getRole(callBackHeartBeatParam);
        String useMd5 = callBackHeartBeatParam.getFrom_Account().toString();
        String key = RedisKeyGenerator.getGroupClassHeartBeatKey(cardId);
        if (StringUtils.equals("STUDENT", role)) {
            zSetOperations.add(key, useMd5, new Date().getTime());
        }
    }
}
