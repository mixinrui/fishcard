package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.requester.resultbean.EventResultBean;
import com.boxfishedu.workorder.web.param.requester.DataAnalysisLogParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by hucl on 16/8/4.
 */
@Component
public class DataAnalysisRequester {
    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 向数据分析组发送获取相关日志接口
     * 目前应该是20秒一次心跳
     */
    public EventResultBean fetchHeartBeatLog(DataAnalysisLogParam dataAnalysisLogParam) {
        String url = String.format("%s/api/log/query/condition?userId=%s&startTime=%s&endTime=%s&event=%s",
                urlConf.getData_analysis_service(), dataAnalysisLogParam.getUserId(), dataAnalysisLogParam.getStartTime(), dataAnalysisLogParam.getEndTime(), dataAnalysisLogParam.getEvent());
        EventResultBean eventResultBean=restTemplate.getForObject(url,EventResultBean.class);
        return eventResultBean;
    }
}
