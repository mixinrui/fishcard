package com.boxfishedu.workorder.service;

import com.boxfishedu.online.order.entity.RedisOrder;
import com.boxfishedu.workorder.common.bean.RedisTypeEnum;
import com.boxfishedu.workorder.dao.redis.RedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/5/18.
 */
@Component
public class RedisService {
    @Autowired
    private RedisRepository redisRepository;
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public <T> void set(T t, RedisTypeEnum redisTypeEnum, Object... params){
        String key=SelfDefKeyGenerator.generateKey(redisTypeEnum,params);
        logger.debug("往redis添加记录,key:[{}]",key);
        redisRepository.set(key,t);
    }

    public <T> T get(Class<T> t,RedisTypeEnum redisTypeEnum,Object... params){
        String key=SelfDefKeyGenerator.generateKey(redisTypeEnum,params);
        return redisRepository.get(key,t);
    }

    public <T> Boolean expire(Class<T> t,RedisTypeEnum redisTypeEnum,Object... params){
        String key=SelfDefKeyGenerator.generateKey(redisTypeEnum,params);
        return redisRepository.expire(key);
    }

    public<T> void getAndSet(T t,RedisTypeEnum redisTypeEnum,Object... params){
        String key=SelfDefKeyGenerator.generateKey(redisTypeEnum,params);
        redisRepository.getAndSet(key,t);
    }

    public <T> void delete(RedisTypeEnum redisTypeEnum,Object... params){
        String key=SelfDefKeyGenerator.generateKey(redisTypeEnum,params);
        logger.debug("从redis中删除,key[{}]",key);
        redisRepository.delete(key);
    }

    public static class SelfDefKeyGenerator{
        public static String generateKey(Object target, String objFlag, Object... params){
            StringBuilder sb = new StringBuilder("BOXFISH_ONLINE_SERVICE-");
            sb.append(target.getClass().getName()).append(("-"));
            sb.append(objFlag).append(("-"));
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        }

        public static String generateKey(RedisTypeEnum redisTypeEnum, Object... params){
            StringBuilder sb = new StringBuilder("BOXFISH_ONLINE_SERVICE-");
            switch (redisTypeEnum){
                case ORDER2SERVICE:
                    sb.append(RedisOrder.class.getName()).append("-");
                    break;
                default:
                    break;
            }
            sb.append(redisTypeEnum.getDesc()).append(("-"));
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        }
    }
}
