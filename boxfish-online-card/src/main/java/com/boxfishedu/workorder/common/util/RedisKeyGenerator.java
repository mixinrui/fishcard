package com.boxfishedu.workorder.common.util;

/**
 * Created by hucl on 17/2/16.
 */
public class RedisKeyGenerator {
    public static String getGroupClassHeartBeatKey(Long smallClassId){
        return "groupkey"+smallClassId;
    }
}
