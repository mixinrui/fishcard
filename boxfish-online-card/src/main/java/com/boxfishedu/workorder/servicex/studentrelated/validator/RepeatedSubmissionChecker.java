package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Created by LuoLiBing on 16/10/18.
 */
@Component
public class RepeatedSubmissionChecker {

    private Cache cache;

    @Autowired
    public RepeatedSubmissionChecker(CacheManager cacheManager) {
        cache = cacheManager.getCache(CacheKeyConstant.WORKORDERS_REPEATED_SUBMISSION);
    }

    /**
     * 当是重复提交时返回true,不是重复提交时返回false,并且保存
     * @param serviceId
     * @return
     */
    public boolean checkRepeatedSubmission(Long serviceId) {
        // 不存在的时候返回false,存在的时候返回true
        return (cache.putIfAbsent(serviceId, true) != null);
    }

    public void evictRepeatedSubmission(Long serviceId) {
        cache.evict(serviceId);
    }
}
