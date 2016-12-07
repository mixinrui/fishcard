package com.boxfishedu.workorder.common.redis;

/**
 * Created by hucl on 16/6/20.
 */
public class CacheKeyConstant {
    public static final String NOTIFY_TEACHEZr_PREPARE_CLASS_KEY="NOTIFY_TEACHEZr_PREPARE_CLASS_KEY";

    /**   用于存储鱼卡后台 用户信息   key  user_code : value {password: , token: }     token加密之前  user_code + 时间  以-分割 **/
    public static final String FISHCARD_BACK_ORDER_USERINFO="FISHCARD_BACK_ORDER_USERINFO";

    /**  存储抢单信息中  教师对应的鱼卡列表  **/
    public static final String FISHCARD_WORKORDER_GRAB_KEY="FISHCARD_WORKORDER_GRAB_KEY";

    public static final String NOTIFY_TEACHER_PREPARE_CLASS_KEY="NOTIFY_TEACHER_PREPARE_CLASS_KEY";

    public static final String TIME_LIMIT_RULES_CACHE_KEY="TIME_LIMIT_RULES_CACHE_KEY";

    public static final String WORKORDERS_REPEATED_SUBMISSION = "WORKORDERS_REPEATED_SUBMISSION";

    public static final String WORKORDERS_INSTANT_CLASS = "INSTANT_CLASS";

    public static final String BASE_TIME_SLOTS = "BASE_TIME_SLOTS";

    public static final String SCHEDULE_HAS_MORE_HISTORY = "SCHEDULE_HAS_MORE_HISTORY";
}
