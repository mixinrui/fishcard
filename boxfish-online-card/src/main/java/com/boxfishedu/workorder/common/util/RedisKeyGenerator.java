package com.boxfishedu.workorder.common.util;

import com.boxfishedu.workorder.common.redis.CacheKeyConstant;

/**
 * Created by hucl on 17/2/16.
 */
public class RedisKeyGenerator {
    public static String getGroupClassHeartBeatKey(Long smallClassId) {
        return CacheKeyConstant.SMALL_CLASS_HEART_BEAT_KEY + smallClassId;
    }

    public static String getTeacherOperationKey(Long smallClassId) {
        return CacheKeyConstant.TEACHER_OPERATION_KEY + smallClassId;
    }

    public static String getGroupSelectedKey(Long smallClassId) {
        return CacheKeyConstant.STUDENT_PICKED_KEY + smallClassId;
    }
}
