package com.boxfishedu.workorder.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/4/7.
 */
@Component
public class PoolConf {
    @Value("${threadpool.httprequest.size_core_pool}")
    private String size_core_pool;
    @Value("${threadpool.httprequest.size_max_pool}")
    private String size_max_pool;
    @Value("${threadpool.httprequest.time_keep_alive}")
    private String time_keep_alive;
    @Value("${threadpool.httprequest.size_work_queue}")
    private String size_work_queue;
    @Value("${threadpool.httprequest.period_task_qos}")
    private String period_task_qos;

    public Integer getSize_core_pool() {
        return Integer.parseInt(size_core_pool);
    }

    public Integer getSize_max_pool() {
        return Integer.parseInt(size_max_pool);
    }

    public Integer getTime_keep_alive() {
        return Integer.parseInt(time_keep_alive);
    }

    public Integer getSize_work_queue() {
        return Integer.parseInt(size_work_queue);
    }

    public Integer getPeriod_task_qos() {
        return Integer.parseInt(period_task_qos);
    }
}
