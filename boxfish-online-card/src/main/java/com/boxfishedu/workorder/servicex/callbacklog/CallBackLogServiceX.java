package com.boxfishedu.workorder.servicex.callbacklog;

import com.alibaba.fastjson.JSON;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.common.util.RedisKeyGenerator;
import com.boxfishedu.workorder.servicex.bean.CallBackBeanEnum;
import com.boxfishedu.workorder.web.param.callbacklog.CallBackHeartBeatParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hucl on 17/2/16.
 */
@SuppressWarnings("ALL")
@Service
public class CallBackLogServiceX {
    private RedisTemplate<String, String> redisTemplate;

    private ZSetOperations<String, String> zSetOperations;

    private ListOperations<String, String> listTeacherOperations;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public void initRedis(@Qualifier(value = "stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        zSetOperations = redisTemplate.opsForZSet();
        listTeacherOperations = redisTemplate.opsForList();
    }

    public Long getCardId(CallBackHeartBeatParam callBackHeartBeatParam) {
        List<CallBackHeartBeatParam.CallBackMsgBody> callBackMsgBodies = callBackHeartBeatParam.getMsgBody();
        Long cardId = -1l;
        for (int i = 0; i < callBackMsgBodies.size(); i++) {
            try {
                CallBackHeartBeatParam.CallBackMsgBody callBackMsgBody = callBackMsgBodies.get(i);
                Map<String, Object> map = JacksonUtil.readValue(callBackMsgBody.getMsgContent().getData(), HashMap.class);
                Object objectCardId = map.get("workOrderId");
                if (Objects.isNull(objectCardId)) {
                    continue;
                }
                cardId = Long.parseLong(objectCardId.toString());
            }
            catch (Exception ex){
                continue;
            }

        }
        return cardId;
    }

    public void updateSet(CallBackHeartBeatParam callBackHeartBeatParam) {
        if (JSON.toJSONString(callBackHeartBeatParam).contains(CallBackBeanEnum.heartbeat.name())) {
            updateHeartBeatSet(callBackHeartBeatParam);
        } else {
            updateTeacherCommands(callBackHeartBeatParam);
        }
    }

    public void updateTeacherCommands(CallBackHeartBeatParam callBackHeartBeatParam) {
        if (!StringUtils.equals("TEACHER", this.getRole(callBackHeartBeatParam))) {
            logger.debug("@updateTeacherCommands#不是教师角色,不作更新[{}]", JacksonUtil.toJSon(callBackHeartBeatParam));
            return;
        }

        Long pageIndex = getPageIndex(callBackHeartBeatParam);
        if (-1 == pageIndex) {
            logger.debug("@updateTeacherCommands#没获取到页码,不作更新[{}]", JacksonUtil.toJSon(callBackHeartBeatParam));
            return;
        }

        Long cardId = getCardId(callBackHeartBeatParam);
        if (-1 == cardId) {
            logger.debug("@updateTeacherCommands#没获取到card的id,不作更新[{}]", JacksonUtil.toJSon(callBackHeartBeatParam));
            return;
        }

        String key = RedisKeyGenerator.getTeacherOperationKey(cardId);

        listTeacherOperations.leftPush(key, pageIndex.toString());

    }

    public void updateHeartBeatSet(CallBackHeartBeatParam callBackHeartBeatParam) {
        Long cardId = this.getCardId(callBackHeartBeatParam);
        String role = this.getRole(callBackHeartBeatParam);
        String useMd5 = callBackHeartBeatParam.getFrom_Account().toString();
        String key = RedisKeyGenerator.getGroupClassHeartBeatKey(cardId);
        if (StringUtils.equals("STUDENT", role)) {
            if (!useMd5.contains("error")) {
                zSetOperations.add(key, useMd5, new Date().getTime());
            }
        }
    }

    private String getRole(CallBackHeartBeatParam callBackHeartBeatParam) {
        List<CallBackHeartBeatParam.CallBackMsgBody> callBackMsgBodies = callBackHeartBeatParam.getMsgBody();
        String role = null;
        for (CallBackHeartBeatParam.CallBackMsgBody callBackMsgBody : callBackMsgBodies) {
            try {
                Map<String, Object> map = JacksonUtil.readValue(callBackMsgBody.getMsgContent().getData(), HashMap.class);
                Object roleObject = map.get("role");
                if (Objects.isNull(roleObject)) {
                    continue;
                }
                role = roleObject.toString();
            } catch (Exception ex) {
                continue;
            }
        }
        return role;
    }

    private Long getPageIndex(CallBackHeartBeatParam callBackHeartBeatParam) {
        List<CallBackHeartBeatParam.CallBackMsgBody> callBackMsgBodies = callBackHeartBeatParam.getMsgBody();
        Long pageIndex = -1l;
        for (CallBackHeartBeatParam.CallBackMsgBody callBackMsgBody : callBackMsgBodies) {
            try {
                Map<String, Object> map = JacksonUtil.readValue(callBackMsgBody.getMsgContent().getData(), HashMap.class);
                Object objectPageIndex = map.get("pageIndex");
                if (Objects.isNull(objectPageIndex)) {
                    continue;
                }
                pageIndex = Long.parseLong(objectPageIndex.toString());
            }catch (Exception ex){
                continue;
            }
        }
        return pageIndex;
    }
}
