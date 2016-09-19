package com.boxfishedu.workorder.servicex.dataanalysis;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.requester.DataAnalysisRequester;
import com.boxfishedu.workorder.requester.resultbean.EventResultBean;
import com.boxfishedu.workorder.web.param.requester.DataAnalysisLogParam;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/8/6.
 */
@Component
public class FetchHeartBeatServiceX {
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataAnalysisRequester dataAnalysisRequester;

    public EventResultBean fetchHeartBeatLog(DataAnalysisLogParam dataAnalysisLogParam) {
       return dataAnalysisRequester.fetchHeartBeatLog(dataAnalysisLogParam);
    }

    public boolean isOnline(DataAnalysisLogParam dataAnalysisLogParam){
        try {
            String startDateStr=DateUtil.Date2String(new Date(dataAnalysisLogParam.getStartTime()));
            String endDateStr=DateUtil.Date2String(new Date(dataAnalysisLogParam.getEndTime()));
            List<EventResultBean.EventResult> eventResults= fetchHeartBeatLog(dataAnalysisLogParam).getContent();
            boolean result= CollectionUtils.isNotEmpty(eventResults);
            logger.info("@FetchHeartBeatServiceX###isOnline###判断当前用户[{}]在指定时间startDate[{}],endDate[{}]是否在线[{}];",
                    dataAnalysisLogParam.getUserId(),startDateStr,endDateStr,result);
            return result;
        }
        catch (Exception ex){
            logger.error("@isOnline分析数据有问题,影响结果");
            return false;
        }

    }
}
