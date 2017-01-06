package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Object getTeacher(Long studentId) {
        return null;
    }
}
