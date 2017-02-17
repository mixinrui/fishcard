package com.boxfishedu.workorder.common.util;

/**
 * Created by hucl on 17/2/16.
 */
public class RedisKeyGenerator {
    public static String getGroupClassHeartBeatKey(Long smallClassId){
        return "groupkey"+smallClassId;
    }

    public static String getGroupSelectedKey(Long smallClassId){
        return "group:selected:"+smallClassId;
    }
}
