package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.requester.resultbean.EventResultBean;
import com.boxfishedu.workorder.web.param.requester.DataAnalysisLogParam;
import com.boxfishedu.workorder.requester.resultbean.NetSourceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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

    private final Integer defaultSize = 100;

    /**
     * 向数据分析组发送获取相关日志接口
     * 目前应该是20秒一次心跳
     */
    public EventResultBean fetchHeartBeatLog(DataAnalysisLogParam dataAnalysisLogParam) {
        String url = String.format("%s/api/log/query/condition?userId=%s&startTime=%s&endTime=%s&event=%s",
                urlConf.getData_analysis_service(), dataAnalysisLogParam.getUserId()
                , dataAnalysisLogParam.getStartTime(), dataAnalysisLogParam.getEndTime(), dataAnalysisLogParam.getEvent());
        EventResultBean eventResultBean = null;
        try {
            logger.info("@fetchHeartBeatLog#invoke开始调用数据分析组数据,url[{}]", url);
            eventResultBean = restTemplate.getForObject(url, EventResultBean.class);
            logger.info("@fetchHeartBeatLog#result开始调用数据分析组数据,result[{}]", JacksonUtil.toJSon(eventResultBean));
            if (null == eventResultBean) {
                throw new BusinessException("@fetchHeartBeatLog#null返回数据为空");
            }
        } catch (Exception ex) {
            logger.error("@fetchHeartBeatLog#fail_invoke调用数据分析失败,url[{}]#将会影响判断的结果", url);
        }
        return eventResultBean;
    }

    public NetSourceBean getNetSourceBean(DataAnalysisLogParam dataAnalysisLogParam, Pageable pageable) {

        String url = String.format("%s/api/log/query/condition?userId=%s&startTime=%s&endTime=%s&event=%s&size=%s",
                urlConf.getData_analysis_service(), dataAnalysisLogParam.getUserId()
                , dataAnalysisLogParam.getStartTime(), dataAnalysisLogParam.getEndTime()
                , dataAnalysisLogParam.getEvent(), pageable.getPageSize());

        NetSourceBean netSourceBean = null;
        try {
            logger.debug("@getNetSourceBean#request#url[{}]", url);
            netSourceBean = restTemplate.getForObject(url, NetSourceBean.class);
            logger.info("@getNetSourceBean#request#url[{}]#result[{}]", url, JacksonUtil.toJSon(netSourceBean));
            if (null == netSourceBean) {
                throw new BusinessException("@fetchHeartBeatLog#null返回数据为空");
            }
        } catch (Exception ex) {
            logger.error("@getNetSourceBean#exception,url[{}]", url, ex);

        }
        return netSourceBean;
    }

}
