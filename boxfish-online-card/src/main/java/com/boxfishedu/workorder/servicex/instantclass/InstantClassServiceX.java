package com.boxfishedu.workorder.servicex.instantclass;

import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.dao.redis.RedisRepository;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/11/3.
 */
@Component
public class InstantClassServiceX {

    @Autowired
    private RedisRepository redisRepository;

    public JsonResultModel instantClass(InstantRequestParam instantRequestParam){
        return null;
    }
}
