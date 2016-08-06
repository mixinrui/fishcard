package com.boxfishedu.workorder.servicex.dataanalysis;

import com.boxfishedu.workorder.requester.DataAnalysisRequester;
import com.boxfishedu.workorder.requester.resultbean.EventResultBean;
import com.boxfishedu.workorder.web.param.requester.DataAnalysisLogParam;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/8/6.
 */
@Component
public class FetchHeartBeatServiceX {

    @Autowired
    private DataAnalysisRequester dataAnalysisRequester;

    public EventResultBean fetchHeartBeatLog(DataAnalysisLogParam dataAnalysisLogParam) {
       return dataAnalysisRequester.fetchHeartBeatLog(dataAnalysisLogParam);
    }

    public boolean isOnline(DataAnalysisLogParam dataAnalysisLogParam){
        List<EventResultBean.EventResult> eventResults= fetchHeartBeatLog(dataAnalysisLogParam).getContent();
        return CollectionUtils.isNotEmpty(eventResults);
    }
}
