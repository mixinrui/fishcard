package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by hucl on 17/1/7.
 */
@Component
public class SmallClassTeacherRequester {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private CacheManager cacheManager;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Object getTeacher(Long studentId) {
        return null;
    }

    @Data
    public static class SmallClassFetchTeacherParam {
        private Long day;
        private String courseType;
        private Long slotId;
        private Integer roleId;
        private Integer countStart;
        private Integer countEnd;
    }
}
