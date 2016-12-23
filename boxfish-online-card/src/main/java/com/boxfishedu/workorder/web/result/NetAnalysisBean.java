package com.boxfishedu.workorder.web.result;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/12/22.
 */
@Data
public class NetAnalysisBean {
    private String maxServicePing;
    private String maxInternetPing;

    private String averageServicePing;
    private String averageSInternetPing;

    private String minServicePing;
    private String minInternetPing;

    private List<NetAnalysisContentBean> contentBeans= Lists.newArrayList();

    @Data
    public static class NetAnalysisContentBean{
        private java.util.Map<String,Object> details= Maps.newHashMap();

        public NetAnalysisContentBean netAdapter(NetSourceBean.ContentBean contentBean){
            NetAnalysisContentBean netAnalysisContentBean=new NetAnalysisContentBean();
            netAnalysisContentBean.setDetails(contentBean.getProperties());
            netAnalysisContentBean.getDetails().put("appTime",new Date(contentBean.getAppTime()));
            netAnalysisContentBean.getDetails().put("sysTime",new Date(contentBean.getSysTime()));
            return netAnalysisContentBean;
        }
    }

}
