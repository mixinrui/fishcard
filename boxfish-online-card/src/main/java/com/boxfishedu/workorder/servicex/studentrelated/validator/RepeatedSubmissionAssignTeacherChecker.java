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
public class RepeatedSubmissionAssignTeacherChecker {

    private Cache cache;

    @Autowired
    public RepeatedSubmissionAssignTeacherChecker(CacheManager cacheManager) {
        cache = cacheManager.getCache(CacheKeyConstant.ASSIGN_REPEATED_SUBMISSION);
    }

    /**
     * 当是重复提交时返回true,不是重复提交时返回false,并且保存
     * @param workOrderId
     * @return
     */
    public boolean checkRepeatedSubmission(Long workOrderId) {
        // 不存在的时候返回false,存在的时候返回true
        return (cache.putIfAbsent(workOrderId, true) != null);
    }

    public void evictRepeatedSubmission(Long orderId) {
        cache.evict(orderId);
    }
}
